package com.websocket.chat.model;

/**
 * Name: ListVO
 * Description: 리스트 VO
 * Creation Date: 2024.03.09
 * */

public class ListVO {

    /** 정렬방식 (e.g. 'ASC' or 'DESC') **/
    private String orderBy;

    /** 현재 페이지 **/
    private Long page;

    /** 한 페이지당 row 수 (e.g. 5 or 10 or 20) **/
    private Long pageByRowCnt;

    /** 현재 페이지의 첫 인덱스 **/
    private Long firstIdx;

    /** 현재 페이지의 마지막 인덱스 **/
    private Long lastIdx;

}
