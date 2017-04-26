package org.n52.schetland.uvf;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTimeZone;
import org.n52.sos.config.SettingDefinition;
import org.n52.sos.config.settings.StringSettingDefinition;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.http.MediaType;

import com.google.common.collect.ImmutableSet;

public interface UVFConstants {

    MediaType CONTENT_TYPE_UVF = new MediaType("application", "uvf");
    
    MediaType CONTENT_TYPE_UVF_WINDOWS = new MediaType("application", "uvf", "lineEnding", "Windows");
    
    MediaType CONTENT_TYPE_UVF_UNIX = new MediaType("application", "uvf", "lineEnding", "Unix");
    
    MediaType CONTENT_TYPE_UVF_MAC = new MediaType("application", "uvf", "lineEnding", "Mac");

    /**
     * Time format to be used in UVF encoded data: <code>yyMMddHHmm</code>,
     * e.g. <code>7001011230</code> is 01.01.1970 12:30 UTC
     */
    String TIME_FORMAT = "yyMMddHHmm";
    
    /**
     * The identifiers length is limited to 15 characters following UVF spec for lines 2, 3
     */
    int MAX_IDENTIFIER_LENGTH = 15;

    /**
     * The maximum length of a value string is limited to 10 characters. Hence, the values are shortened,
     * e.g. <code>52.1234567890</code> will be cut to <code>52.1234567</code>
     */
    int MAX_VALUE_LENGTH = 10;

    /**
     * No data values MUST be encoded with <code>-777</code> in the UVF format.
     */
    String NO_DATA_STRING = "-777";
    
    /**
     * The list of allowed CRS EPSG codes. Here, the German GK bands:
     * <ul>
     * <li>31466</li>
     * <li>31467</li>
     * <li>31468</li>
     * <li>31469</li>
     * </ul>
     */
    List<String> ALLOWED_CRS = Collections.unmodifiableList(CollectionHelper.list("31466", "31467", "31468",
            "31469"));

    int MINIMUM_EPSG_CODE = 31466;

    int MAXIMUM_EPSG_CODE = 31469;
    
    String LINE_ENDING_UNIX = "\n";
    
    String LINE_ENDING_WINDOWS = "\r\n";
    
    String LINE_ENDING_MAC = "\r";
    
    enum LineEnding{
        Windows, Unix, Mac;
    }
}
 