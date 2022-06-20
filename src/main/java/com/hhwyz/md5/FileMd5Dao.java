package com.hhwyz.md5;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FileMd5Dao {
    long countByExample(FileMd5Example example);

    int deleteByExample(FileMd5Example example);

    int deleteByPrimaryKey(Integer id);

    int insert(FileMd5 record);

    int insertSelective(FileMd5 record);

    List<FileMd5> selectByExample(FileMd5Example example);

    FileMd5 selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") FileMd5 record, @Param("example") FileMd5Example example);

    int updateByExample(@Param("record") FileMd5 record, @Param("example") FileMd5Example example);

    int updateByPrimaryKeySelective(FileMd5 record);

    int updateByPrimaryKey(FileMd5 record);
}