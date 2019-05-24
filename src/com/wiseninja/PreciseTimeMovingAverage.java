package com.wiseninja;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class PreciseTimeMovingAverage {
    private CopyOnWriteArrayList<DateTimeWrapper> allSamples = new CopyOnWriteArrayList<>();
    private Duration interval;

    public PreciseTimeMovingAverage(Duration checkInterval)
    {
        interval = checkInterval;
    }

    public void AddSample(float val, LocalDateTime eventTime)
    {
        allSamples.add(new DateTimeWrapper(val, eventTime));
    }

    public double CalculateAverage(LocalDateTime currentTime)
    {
        LocalDateTime borderTime = currentTime.minusMinutes(interval.toMinutes());
        ArrayList<DateTimeWrapper> validSamples = new ArrayList<>();

            if (allSamples.size() != 0)
            {
                while (allSamples.size() != 0 && allSamples.get(0).EventTime.isBefore(borderTime))
                {
                    //we can remove the sample from the collection at this point as it is no longer relevant to the calculation
                    allSamples.remove(0);
                }

                allSamples.forEach( sample ->
                {
                    //we are interested in the samples which are relevant to our time frame
                    if (sample.EventTime.isBefore(currentTime) || sample.EventTime.isEqual(currentTime))
                    {
                        validSamples.add(sample);
                    }
                });
            }

            if (validSamples.size() > 0)
            {
                return validSamples.stream().mapToDouble( a -> a.Value).sum() / validSamples.size();
            }
            return 0;
        }
}
