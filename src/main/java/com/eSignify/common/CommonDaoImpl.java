package com.eSignify.common;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository // 스프링 빈으로 등록
public class CommonDaoImpl implements CommonDao {

    private final SqlSession sqlSession;

    @Autowired
    public CommonDaoImpl(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    @Override
    public <T> T selectOne(String queryId, Map<String, ?> parameter) {
        return sqlSession.selectOne(queryId, parameter);
    }

    @Override
    public <T> T selectOne(String queryId, Object parameter) {
        return sqlSession.selectOne(queryId, parameter);
    }

    @Override
    public Map<String, ?> selectMap(String queryId, Map<String, ?> parameter, String mapKey) {
        return sqlSession.selectMap(queryId, parameter, mapKey);
    }

    @Override
    public List selectList(String queryId, Object parameter) {
        return sqlSession.selectList(queryId, parameter);
    }

    @Override
    public List selectList(String queryId, Map<String, ?> parameter) {
        return sqlSession.selectList(queryId, parameter);
    }

    @Override
    public <T> Cursor<T> selectCursor(String queryId, Map<String, ?> parameter) {
        return sqlSession.selectCursor(queryId, parameter);
    }

    @Override
    public <T> Cursor<T> selectCursor(String queryId, Object parameter) {
        return sqlSession.selectCursor(queryId, parameter);
    }

    @Override
    public int insert(String queryId, Map<String, ?> parameter) {
        return sqlSession.insert(queryId, parameter);
    }

    @Override
    public int insert(String queryId, Object parameter) {
        return sqlSession.insert(queryId, parameter);
    }

    @Override
    public int update(String queryId, Map<String, ?> parameter) {
        return sqlSession.update(queryId, parameter);
    }

    @Override
    public int update(String queryId, Object parameter) {
        return sqlSession.update(queryId, parameter);
    }

    @Override
    public int delete(String queryId, Map<String, ?> parameter) {
        return sqlSession.delete(queryId, parameter);
    }

    @Override
    public int delete(String queryId, Object parameter) {
        return sqlSession.delete(queryId, parameter);
    }

    @Override
    public int call(String queryId, Map<String, ?> parameter) {
        return sqlSession.update(queryId, parameter); // 주로 update()로 저장 프로시저 호출
    }

    @Override
    public int call(String queryId, Object parameter) {
        return sqlSession.update(queryId, parameter); // 주로 update()로 저장 프로시저 호출
    }

    @Override
    public int insertBatch(String queryId, List<?> parameterList) {
        int count = 0;
        for (Object parameter : parameterList) {
            count += sqlSession.insert(queryId, parameter);
        }
        return count;
    }

    @Override
    public int updateBatch(String queryId, List<?> parameterList) {
        int count = 0;
        for (Object parameter : parameterList) {
            count += sqlSession.update(queryId, parameter);
        }
        return count;
    }

    @Override
    public int deleteBatch(String queryId, List<?> parameterList) {
        int count = 0;
        for (Object parameter : parameterList) {
            count += sqlSession.delete(queryId, parameter);
        }
        return count;
    }
}
