package com.vanguard.weatherapi.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherDto {
    @ApiModelProperty(example = "Melbourne")
    private String city;
    @ApiModelProperty(example = "Australia")
    private String country;
    @ApiModelProperty(example = "Cloudy", notes = "The weather description")
    private String description;
}
