package com.tickcore.booking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.booking")
public class BookingProperties {

    // số giờ tối thiểu trước giờ chụp.
    private int minAdvanceHours = 12;

    // số giờ tối thiểu trước giờ chụp còn có thể hủy.
    private int cancelDeadlineHours = 24;

}
