package com.company;
        import java.time.*;
public class DateTimeWrapper {

    public float Value;
    public LocalDateTime EventTime;

    public DateTimeWrapper(float aValue, LocalDateTime eventTime)
    {
        Value = aValue;
        EventTime = eventTime;
    }

}
