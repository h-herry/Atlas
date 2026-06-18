package com.atlas.common.security.interceptor;

import com.atlas.common.security.annotation.DataScope;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 数据权限拦截器 — MyBatis Plugin，根据 @DataScope 注解自动拼接 SQL 过滤条件 /
 * Data scope interceptor — MyBatis Plugin, auto-appends SQL filter conditions based on @DataScope annotation
 * <p>
 * 数据范围 / Data scope：
 * <ul>
 *   <li>SELF  — WHERE created_by = userId（仅本人数据 / self data only）</li>
 *   <li>DEPT  — WHERE dept_id = userDeptId（本部门数据 / department data）</li>
 *   <li>CUSTOM— WHERE dept_id IN (自定义部门列表 / custom department list)</li>
 *   <li>ALL   — 不加条件 / no filter</li>
 * </ul>
 */
@Slf4j
public class DataScopeInterceptor implements InnerInterceptor {

    private static final String DATA_SCOPE_SELF = "SELF";
    private static final String DATA_SCOPE_DEPT = "DEPT";
    private static final String DATA_SCOPE_ALL = "ALL";
    private static final String DATA_SCOPE_CUSTOM = "CUSTOM";

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        if (ms.getSqlCommandType() != SqlCommandType.SELECT) {
            return;
        }

        // 获取 Mapper 方法上的 @DataScope 注解 / Get @DataScope annotation on Mapper method
        DataScope annotation = getDataScopeAnnotation(ms);
        if (annotation == null) {
            return;
        }

        // 获取当前用户信息 / Get current user info
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return;
        }

        Long userId = null;
        Long userDeptId = null;
        String dataScope = null;
        List<Long> customDeptIds = null;

        // 从 Authentication.details (Claims) 中提取 deptId 和 dataScope / Extract deptId and dataScope from Authentication.details (Claims)
        if (auth.getDetails() instanceof Claims claims) {
            Object deptIdObj = claims.get("deptId");
            if (deptIdObj instanceof Number) {
                userDeptId = ((Number) deptIdObj).longValue();
            }
            Object scopeObj = claims.get("dataScope");
            if (scopeObj != null) {
                dataScope = scopeObj.toString();
            }
            userId = Long.valueOf(claims.getSubject());
        }

        if (dataScope == null || DATA_SCOPE_ALL.equalsIgnoreCase(dataScope)) {
            return; // 全部数据权限，不加条件 / Full data scope, no filter
        }

        // 构造 SQL 条件 / Build SQL condition
        Expression scopeCondition = buildScopeCondition(annotation, dataScope, userId, userDeptId, customDeptIds);
        if (scopeCondition == null) {
            return;
        }

        // 注入到 SQL 的 WHERE 子句 / Inject into WHERE clause
        try {
            String originalSql = boundSql.getSql();
            net.sf.jsqlparser.parser.CCJSqlParserUtil.parse("SELECT 1 FROM DUAL"); // warm-up parser
            Select select = (Select) net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(originalSql);
            Select selectBody = select.getSelectBody();

            if (selectBody instanceof PlainSelect plainSelect) {
                Expression where = plainSelect.getWhere();
                if (where == null) {
                    plainSelect.setWhere(scopeCondition);
                } else {
                    plainSelect.setWhere(new AndExpression(where, scopeCondition));
                }
                // 反射替换 boundSql 中的 SQL（MyBatis BoundSql 的 sql 字段） / Reflectively replace SQL in boundSql (MyBatis BoundSql.sql field)
                java.lang.reflect.Field sqlField = BoundSql.class.getDeclaredField("sql");
                sqlField.setAccessible(true);
                sqlField.set(boundSql, select.toString());
            }
        } catch (Exception e) {
            log.error("DataScopeInterceptor SQL注入失败: msId={}", ms.getId(), e);
        }
    }

    /**
     * 根据数据范围类型构造 WHERE 条件 / Build WHERE condition based on data scope type
     */
    private Expression buildScopeCondition(DataScope annotation, String dataScope,
                                            Long userId, Long deptId, List<Long> customDeptIds) {
        switch (dataScope.toUpperCase()) {
            case DATA_SCOPE_SELF:
                return new EqualsTo(
                        new Column(annotation.userColumn()),
                        new LongValue(userId));
            case DATA_SCOPE_DEPT:
                return new EqualsTo(
                        new Column(annotation.deptColumn()),
                        new LongValue(deptId));
            case DATA_SCOPE_CUSTOM:
                if (customDeptIds == null || customDeptIds.isEmpty()) {
                    // 未配置自定义部门则退化为仅本人 / Fallback to self only if no custom dept configured
                    return new EqualsTo(
                            new Column(annotation.userColumn()),
                            new LongValue(userId));
                }
                ExpressionList<Expression> exprList = new ExpressionList<>();
                for (Long id : customDeptIds) {
                    exprList.addExpressions(new LongValue(id));
                }
                return new InExpression(
                        new Column(annotation.deptColumn()),
                        exprList);
            default:
                return null;
        }
    }

    /**
     * 从 MappedStatement 获取 Mapper 方法上的 @DataScope 注解 / Get @DataScope annotation from MappedStatement
     */
    private DataScope getDataScopeAnnotation(MappedStatement ms) {
        try {
            String msId = ms.getId();
            String className = msId.substring(0, msId.lastIndexOf('.'));
            String methodName = msId.substring(msId.lastIndexOf('.') + 1);
            Class<?> mapperClass = Class.forName(className);
            for (Method method : mapperClass.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.isAnnotationPresent(DataScope.class)) {
                    return method.getAnnotation(DataScope.class);
                }
            }
        } catch (Exception e) {
            log.debug("获取 @DataScope 注解失败: msId={}", ms.getId(), e);
        }
        return null;
    }
}
