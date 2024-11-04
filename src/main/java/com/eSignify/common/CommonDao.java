/**
 *  Description : Repository 기반 CRUD (Create, Retrieve, Update, Delete) 작업 수행을 위한 공통 DAO 인터페이스
 *  Modification Information
 * 
 *     수정일         수정자                   수정내용
 *   -------    --------    ---------------------------
 *   2022.07.14    서경진          최초 생성
 *
 *  @author AA 서경진
 *  @since 2022. 07. 14
 *  @version 1.0
 *  @see 
 * 
 *  Copyright (C) 2022 by Seegene.mf  All right reserved.
 */
package com.eSignify.common;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.cursor.Cursor;

/**
 * 
 * CRUD 작업을 지원하는 공통 DAO 인터페이스.
 * 
 */
public interface CommonDao {
   
   public <T> T selectOne(String queryId, Map<String, ?> parameter);
   
   public <T> T selectOne(String queryId, Object parameter);

   public Map<String, ?> selectMap(String queryId, Map<String, ?> parameter, String mapKey);
    
    public List selectList(String queryId, Object parameter);
    
    public List selectList(String queryId, Map<String, ?> parameter);
    
    public <T> Cursor<T> selectCursor(String queryId, Map<String, ?> parameter);
    
    public <T> Cursor<T> selectCursor(String queryId, Object parameter);
    
    public int insert(String queryId, Map<String, ?> parameter);
    
    public int insert(String queryId, Object parameter);
    
    public int update(String queryId, Map<String, ?> parameter);
    
    public int update(String queryId, Object parameter);
    
   public int delete(String queryId, Map<String, ?> parameter);
   
   public int delete(String queryId, Object parameter);
   
   public int call(String queryId, Map<String, ?> parameter);
   
   public int call(String queryId, Object parameter);
   
   public int insertBatch(String queryId, List<?> parameterList);
   
   public int updateBatch(String queryId, List<?> parameterList);
   
   public int deleteBatch(String queryId, List<?> parameterList);

}
