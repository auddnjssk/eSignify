package com.eSignify.common;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 객체 관련 기능을 제공하는 유틸리티 클래스.
 *
 * @author kyong94
 */
@Slf4j
public abstract class ObjectUtil extends org.apache.commons.lang3.ObjectUtils {

  /**
   * Empty 여부를 확인한다.
   * 
   * @param o Object
   * @return boolean
   * @exception IllegalArgumentException
   */
  public static boolean isEmpty(Object o) throws IllegalArgumentException {
    try {
      if (o == null)
        return true;

      if (o instanceof String) {
        if (((String) o).length() == 0) {
          return true;
        }
      } else if (o instanceof Collection) {
        if (((Collection) o).isEmpty()) {
          return true;
        }
      } else if (o.getClass().isArray()) {
        if (Array.getLength(o) == 0) {
          return true;
        }
      } else if (o instanceof Map) {
        if (((Map) o).isEmpty()) {
          return true;
        }
      } else {
        return false;
      }

      return false;
      // 2017.03.03 조성원 시큐어코딩(ES)-오류 메시지를 통한 정보노출[CWE-209]
    } catch (IllegalArgumentException e) {
      log.error(
          "[IllegalArgumentException] Try/Catch...usingParameters Runing : " + e.getMessage());
    } catch (Exception e) {
      log.error("[" + e.getClass() + "] Try/Catch...Exception : " + e.getMessage());
    }
    return false;
  }

  /**
   * Not Empty 여부를 확인한다.
   * 
   * @param o Object
   * @return boolean
   * @exception IllegalArgumentException
   */
  public static boolean isNotEmpty(Object o) {
    return !isEmpty(o);
  }

  /**
   * Equal 여부를 확인한다.
   * 
   * @param obj Object, obj Object
   * @return boolean
   */

  public static boolean isEquals(Object obj, Object obj2) {
    if (isEmpty(obj))
      return false;

    if (obj instanceof String && obj2 instanceof String) {
      if ((String.valueOf(obj)).equals(String.valueOf(obj2))) {
        return true;
      }
    } else if (obj instanceof String && obj2 instanceof Character) {
      if ((String.valueOf(obj)).equals(String.valueOf(obj2))) {
        return true;
      }
    } else if (obj instanceof String && obj2 instanceof Integer) {
      if ((String.valueOf(obj)).equals(String.valueOf((Integer) obj2))) {
        return true;
      }

    } else if (obj instanceof Integer && obj2 instanceof String) {
      if ((String.valueOf(obj2)).equals(String.valueOf((Integer) obj))) {
        return true;
      }
    } else if (obj instanceof Integer && obj instanceof Integer) {
      if ((Integer) obj == (Integer) obj2) {
        return true;
      }
    }

    return false;
  }

  /**
   * String의 Equal 여부를 확인한다.
   * 
   * @param obj Object, obj Object
   * @return boolean
   */
  public static boolean isEqualsStr(Object obj, String s) {
    if (isEmpty(obj))
      return false;

    if (s.equals(String.valueOf(obj))) {
      return true;
    }
    return false;
  }

  public static Map<String, Object> convertObjectToMap(Object obj) {
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, Object> map = objectMapper.convertValue(obj,Map.class);
    return new HashMap<>(map);
  }

  public static Map converObjectToMap(Object obj) {
    ObjectMapper objectMapper = new ObjectMapper();
    Map map = objectMapper.convertValue(obj,Map.class);
    return new HashMap<>(map);
  }


  public static Object convertMapToObject(Map map, Object objClass) {
    String keyAttribute = null;
    String setMethodString = "set";
    String methodString = null;
    Iterator itr = map.keySet().iterator();
    while (itr.hasNext()) {
      keyAttribute = (String) itr.next();
      methodString =
          setMethodString + keyAttribute.substring(0, 1).toUpperCase() + keyAttribute.substring(1);
      try {
        Method[] methods = objClass.getClass().getDeclaredMethods();
        Method[] superMethods = objClass.getClass().getSuperclass().getDeclaredMethods();
        if (superMethods.length != 0) {
          for (int i = 0; i <= superMethods.length - 1; i++) {
            if (methodString.equals(superMethods[i].getName())) {
              // System.out.println("invoke : " + methodString);
              superMethods[i].invoke(objClass, map.get(keyAttribute));
            }
          }
        }
        for (int i = 0; i <= methods.length - 1; i++) {
          if (methodString.equals(methods[i].getName())) {
            // System.out.println("invoke : " + methodString);
            methods[i].invoke(objClass, map.get(keyAttribute));
          }
        }
      } catch (SecurityException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
    return objClass;
  }

  /**
   * 입력 객체가 배열 클래스인지 여부를 리턴함.
   *
   * @param object 객체
   * @return 입력 객체가 배열 클래스이면 <code>true</code>. 그 이외에는 <code>false</code>.
   */
  public static boolean isArray(Object object) {
    return object != null && object.getClass().isArray();
  }

  /**
   * 역직렬화하여 객체로 변환함.
   *
   * @param inputStream 변환할 객체
   * @return 변환된 객체
   * @throws IOException I/O 에러 발생할 때
   * @throws ClassNotFoundException 객체를 찾을 수 없을 때
   */
  public static Object deserialize(InputStream inputStream)
      throws IOException, ClassNotFoundException {
    return new ObjectInputStream(inputStream).readUnshared();
  }



}
