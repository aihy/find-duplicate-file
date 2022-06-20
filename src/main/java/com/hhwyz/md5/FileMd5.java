package com.hhwyz.md5;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * file_md5
 *
 * @author
 */
@Data
public class FileMd5 implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private Date gmtCreate;
    private Date gmtModified;
    private String path;
    private String md5;
    private Long size;
}