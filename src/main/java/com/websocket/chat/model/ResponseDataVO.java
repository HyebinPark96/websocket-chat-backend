package com.websocket.chat.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

/**
 * Name: ResultDataVO
 * Description: 응답 데이터 VO
 * Creation Date: 2024.03.09
 * */

@Getter
@Setter
@AllArgsConstructor
public class ResponseDataVO<T> {

    /** 응답 데이터 **/
    private T data;

}
