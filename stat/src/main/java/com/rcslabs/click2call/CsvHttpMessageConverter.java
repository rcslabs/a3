package com.rcslabs.click2call;

import com.rcslabs.click2call.csv.CsvBuilder;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

public class CsvHttpMessageConverter extends AbstractHttpMessageConverter<CsvBuilder>
        implements GenericHttpMessageConverter<CsvBuilder> {

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    public CsvHttpMessageConverter() {
        super(new MediaType("text", "csv", DEFAULT_CHARSET));
    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return true;
    }

    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        return true;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return (clazz.getName().endsWith("CsvBuilder") && canWrite(mediaType));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        // should not be called, since we override canRead/Write instead
        throw new UnsupportedOperationException();
    }

    @Override
    protected CsvBuilder readInternal(Class<? extends CsvBuilder> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        return null;
    }

    public CsvBuilder read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        return null;
    }

    @Override
    protected void writeInternal(CsvBuilder object, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        outputMessage.getHeaders().add("Content-Disposition", "attachment; filename=\""+object.getFilename()+"\"");
        outputMessage.getBody().write( object.getResult().getBytes() );
    }
}
