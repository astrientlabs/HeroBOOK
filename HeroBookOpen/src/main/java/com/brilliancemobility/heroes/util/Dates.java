/*******************************************************************************
 * Copyright (c) 2009 Licensed under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except in compliance 
 * with the License. You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Contributors:
 * 
 * Astrient Foundation Inc. 
 * www.astrientfoundation.org
 * rashid@astrientfoundation.org
 * Rashid Mayes 2009
 *******************************************************************************/
package com.brilliancemobility.heroes.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class Dates
{
    private static boolean lenient;
    
    private Dates()
    {
    }
    
    public static String format(long milliseconds, String format)
    {
        return format(new Date(milliseconds),format);
    }
    
    public static String format(Date date, String format)
    {
        return format(date,format,Locale.US);
    }
    
    public static String format(Date date, String format, Locale locale)
    {
        return format(date,format,null,locale);
    }   
    
    public static String format(Date date, String format, String nullmask)
    {
        return format(date, format, nullmask, Locale.US);
    }   
    
    public static SimpleDateFormat getSimpleDateFormat(String format)
    {
        return getSimpleDateFormat(format,Locale.getDefault());
    }
     
    public static SimpleDateFormat getSimpleDateFormat(String format,Locale locale)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(format,locale);
        sdf.setLenient(lenient);
        
        return sdf;     
    }

    
    public static String format(Date date, String format, String nullmask, Locale locale)
    {
        SimpleDateFormat sdf = getSimpleDateFormat(format);

        try
        {
            return sdf.format(date);
        }
        catch (Exception e)
        {
            return nullmask;
        }
    }
    
    
    public static Date parse(String dateStr, String format)
    {
        String[] formats = { format };
        return parse(dateStr,formats);
    }
    
    public static Date parse(String dateStr, String[] formats)
    {
        SimpleDateFormat sdf;

        for (int i = 0; i < formats.length; i++)
        {
            sdf = getSimpleDateFormat(formats[i]);
            try
            {
                return sdf.parse(dateStr);
            }
            catch (Exception e)
            {
            }
        }

        return null;
    }

    public static boolean isLenient()
    {
        return lenient;
    }
    
    public static void setLenient(boolean b)
    {
        lenient = b;
    }
     
    public static String format(long milliseconds, String format, TimeZone timeZone)
    {
        return format(new Date(milliseconds),format,timeZone);
    }
    
    public static String format(Date date, String format, TimeZone timeZone)
    {
        return format(date,format,Locale.US, timeZone);
    }
    
    public static String format(Date date, String format, Locale locale, TimeZone timeZone)
    {
        return format(date,format,null,locale, timeZone);
    }   
    
    public static String format(Date date, String format, String nullmask, TimeZone timeZone)
    {
        return format(date, format, nullmask, Locale.US, timeZone);
    }   
    
    public static SimpleDateFormat getSimpleDateFormat(String format, TimeZone timeZone)
    {
        return getSimpleDateFormat(format,Locale.getDefault(),timeZone);
    }
     
    public static SimpleDateFormat getSimpleDateFormat(String format,Locale locale, TimeZone timeZone)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(format,locale);
        sdf.setLenient(lenient);
        sdf.setTimeZone(timeZone);
        
        return sdf;     
    }

    
    public static String format(Date date, String format, String nullmask, Locale locale, TimeZone timeZone)
    {
        SimpleDateFormat sdf = getSimpleDateFormat(format,timeZone);

        try
        {
            return sdf.format(date);
        }
        catch (Exception e)
        {
            return nullmask;
        }
    }
    
    
    public static Date parse(String dateStr, String format, TimeZone timeZone)
    {
        String[] formats = { format };
        return parse(dateStr,formats, timeZone);
    }
    
    public static Date parse(String dateStr, String[] formats, TimeZone timeZone)
    {
        SimpleDateFormat sdf;

        for (int i = 0; i < formats.length; i++)
        {
            sdf = getSimpleDateFormat(formats[i],timeZone);
            try
            {
                return sdf.parse(dateStr);
            }
            catch (Exception e)
            {
            }
        }

        return null;
    }
}
