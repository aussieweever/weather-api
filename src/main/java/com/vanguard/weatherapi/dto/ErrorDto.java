package com.vanguard.weatherapi.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDto {
    @ApiModelProperty(example = "City Whitehorse not found")
    private String errorMessage;
}
