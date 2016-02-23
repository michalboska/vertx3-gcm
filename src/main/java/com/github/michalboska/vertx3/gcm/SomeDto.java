package com.github.michalboska.vertx3.gcm;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.io.Serializable;

@DataObject
public class SomeDto {

    private String string1, string2;
    private Long long1, long2;

    public SomeDto() {
    }

    public SomeDto(SomeDto someDto) {

    }

    public SomeDto(JsonObject jsonObject) {

    }

    public SomeDto(String string1, String string2, Long long1, Long long2) {
        this.string1 = string1;
        this.string2 = string2;
        this.long1 = long1;
        this.long2 = long2;
    }


    public String getString1() {
        return string1;
    }

    public String getString2() {
        return string2;
    }

    public Long getLong1() {
        return long1;
    }

    public Long getLong2() {
        return long2;
    }

    public JsonObject toJson() {
        return new JsonObject();
    }
}
