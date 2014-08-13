package com.brilliancemobility.heroes.util;

import java.util.Locale;

import org.joda.time.Period;
import org.joda.time.PeriodType;

import android.content.res.Resources;
import com.brilliancemobility.heroes.R;

public class Days
{
    public static String getDaysString(Resources resources, long start, Locale locale)
    {
        long now = System.currentTimeMillis();

        if ( start > now  )
        {
            Period period = new Period(now,start,PeriodType.days());
            if ( period.getDays() < 2 )
            {
                period = new Period(now,start,PeriodType.hours());
                if ( period.getHours() == 0 )
                {
                    period = new Period(now,start,PeriodType.minutes());
                    if ( period.getMinutes() == 0 )
                    {
                        return resources.getString(R.string.now);       
                    }
                    else
                    {
                        return resources.getString(R.string.minutesfromnow, new Object[] { period.getMinutes() });    
                    }
                }
                else
                {
                    return resources.getString(R.string.hoursfromnow, new Object[] { period.getHours() });
                }
            }
            /*else if ( period.getDays() == 1 )
            {
                period = new Period(now,start,PeriodType.hours());
                if ( period.getHours() <= 24 )
                {
                    return resources.getString(R.string.tomorrow", locale);
                }
                else
                {
                    
                }            
            }*/
            return resources.getString(R.string.daysfromnow, new Object[] { period.getDays() });
        }
        else
        {
            Period period = new Period(start,now,PeriodType.days());
            if ( period.getDays() < 2 )
            {
                period = new Period(start,now,PeriodType.hours());
                if ( period.getHours() == 0 )
                {
                    period = new Period(start,now,PeriodType.minutes());
                    if ( period.getMinutes() == 0 )
                    {
                        return resources.getString(R.string.lessthanoneminuteago);    
                    }
                    else
                    {
                        return resources.getString(R.string.minutesago, new Object[] { period.getMinutes() });
                    }
                }
                else
                {
                    return resources.getString(R.string.hoursago, new Object[] { period.getHours() });
                }
                
                
            }
            /*else if ( period.getDays() == 1 )
            {
                return resources.getString(R.string.yesterday", locale);
            }*/
            return resources.getString(R.string.daysago, new Object[] { period.getDays() });            
        }
    }
    
    public static String getDaysString(Resources resources, long start, Locale locale, long max, String format)
    {
        long now = System.currentTimeMillis();

        if ( start > now  )
        {
            Period period = new Period(now,start,PeriodType.days());
            if ( period.getDays() < 2 )
            {
                period = new Period(now,start,PeriodType.hours());
                if ( period.getHours() == 0 )
                {
                    period = new Period(now,start,PeriodType.minutes());
                    if ( period.getMinutes() == 0 )
                    {
                        return resources.getString(R.string.now);       
                    }
                    else
                    {
                        return resources.getString(R.string.minutesfromnow, new Object[] { period.getMinutes() });    
                    }
                }
                else
                {
                    return resources.getString(R.string.hoursfromnow, new Object[] { period.getHours() });
                }
            }
            /*else if ( period.getDays() == 1 )
            {
                period = new Period(now,start,PeriodType.hours());
                if ( period.getHours() <= 24 )
                {
                    return resources.getString(R.string.tomorrow", locale);
                }
                else
                {
                    
                }            
            }*/
            
            int days = period.getDays();
            if ( days > max )
            {
            	return Dates.format(start, format);
            }
            else
            {
            	return resources.getString(R.string.daysfromnow, new Object[] { days });	
            } 
        }
        else
        {
            Period period = new Period(start,now,PeriodType.days());
            if ( period.getDays() < 2 )
            {
                period = new Period(start,now,PeriodType.hours());
                if ( period.getHours() == 0 )
                {
                    period = new Period(start,now,PeriodType.minutes());
                    if ( period.getMinutes() == 0 )
                    {
                        return resources.getString(R.string.lessthanoneminuteago);    
                    }
                    else
                    {
                        return resources.getString(R.string.minutesago, new Object[] { period.getMinutes() });
                    }
                }
                else
                {
                    return resources.getString(R.string.hoursago, new Object[] { period.getHours() });
                }
                
                
            }
            /*else if ( period.getDays() == 1 )
            {
                return resources.getString(R.string.yesterday", locale);
            }*/
            
            int days = period.getDays();
            if ( days > max )
            {
            	return Dates.format(start, format);
            }
            else
            {
            	return resources.getString(R.string.daysago, new Object[] { days });	
            }           
        }
    }    
}
