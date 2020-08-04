package hr.hrg.hipster.mongo;

import org.bson.*;
import org.bson.codecs.*;

class EnumCodec<T extends Enum<T>> implements Codec<T> {
    private final Class<T> clazz;

    EnumCodec(final Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void encode(final BsonWriter writer, final T value, final EncoderContext encoderContext) {
        writer.writeString(value.name());
    }

    @Override
    public Class<T> getEncoderClass() {
        return clazz;
    }

    @Override
    public T decode(final BsonReader reader, final DecoderContext decoderContext) {
        return Enum.valueOf(clazz, reader.readString());
    }
}