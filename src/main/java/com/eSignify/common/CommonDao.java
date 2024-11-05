

package com.eSignify.common;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CommonDao {

    private final SqlSession sqlSession;

    @Autowired
    public CommonDao(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    // 단일 결과 조회
    public <T> T selectOne(String queryId, Object parameter) {
        return sqlSession.selectOne(queryId, parameter);
    }

    // 여러 결과 조회
    public <E> List<E> selectList(String queryId, Object parameter) {
        return sqlSession.selectList(queryId, parameter);
    }

    // 데이터 삽입
    public int insert(String queryId, Object parameter) {
        return sqlSession.insert(queryId, parameter);
    }

    // 데이터 업데이트
    public int update(String queryId, Object parameter) {
        return sqlSession.update(queryId, parameter);
    }

    // 데이터 삭제
    public int delete(String queryId, Object parameter) {
        return sqlSession.delete(queryId, parameter);
    }
}