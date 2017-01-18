package org.camunda.tngp.msgpack.spec;

import static org.agrona.BitUtil.SIZE_OF_BYTE;
import static org.agrona.BitUtil.SIZE_OF_FLOAT;
import static org.agrona.BitUtil.SIZE_OF_DOUBLE;
import static org.agrona.BitUtil.SIZE_OF_INT;
import static org.agrona.BitUtil.SIZE_OF_LONG;
import static org.agrona.BitUtil.SIZE_OF_SHORT;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.ARRAY16;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.ARRAY32;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.BIN16;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.BIN32;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.BIN8;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.BYTE_ORDER;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.FALSE;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.FIXARRAY_PREFIX;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.FIXMAP_PREFIX;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.FIXSTR_PREFIX;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.INT16;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.INT32;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.INT64;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.INT8;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.MAP16;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.MAP32;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.NIL;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.STR16;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.STR32;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.STR8;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.TRUE;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.UINT16;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.UINT32;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.UINT64;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.UINT8;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.FLOAT32;
import static org.camunda.tngp.msgpack.spec.MsgPackCodes.FLOAT64;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

/**
 * This class uses signed value semantics. That means, an integer 0xffff_ffff is treated
 * as -1 instead of 2^33 - 1, etc.
 */
public class MsgPackWriter
{
    private MutableDirectBuffer buffer;
    private int offset;

    public MsgPackWriter wrap(MutableDirectBuffer buffer, int offset)
    {
        this.buffer = buffer;
        this.offset = offset;

        return this;
    }

    public MsgPackWriter writeArrayHeader(int size)
    {
        if (size < (1 << 4))
        {
            buffer.putByte(offset, (byte) (FIXARRAY_PREFIX | size));
            ++offset;
        }
        else if (size < (1 << 16))
        {
            buffer.putByte(offset, ARRAY16);
            ++offset;

            buffer.putShort(offset, (short) size, BYTE_ORDER);
            offset += SIZE_OF_SHORT;
        }
        else
        {
            buffer.putByte(offset, ARRAY32);
            ++offset;

            buffer.putInt(offset, size, BYTE_ORDER);
            offset += SIZE_OF_INT;
        }

        return this;
    }


    public MsgPackWriter writeMapHeader(int size)
    {
        if (size < (1 << 4))
        {
            buffer.putByte(offset, (byte) (FIXMAP_PREFIX | size));
            ++offset;
        }
        else if (size < (1 << 16))
        {
            buffer.putByte(offset, MAP16);
            ++offset;

            buffer.putShort(offset, (short) size, MsgPackCodes.BYTE_ORDER);
            offset += SIZE_OF_SHORT;
        }
        else
        {
            buffer.putByte(offset, MAP32);
            ++offset;

            buffer.putInt(offset, size, MsgPackCodes.BYTE_ORDER);
            offset += SIZE_OF_INT;
        }

        return this;
    }

    public MsgPackWriter writeRaw(DirectBuffer buffer)
    {
        return writeRaw(buffer, 0, buffer.capacity());
    }

    public MsgPackWriter writeRaw(DirectBuffer buff, int offset, int length)
    {
        this.buffer.putBytes(this.offset, buff, offset, length);
        this.offset += length;

        return this;
    }

    public MsgPackWriter writeString(DirectBuffer bytes)
    {
        return this.writeString(bytes, 0, bytes.capacity());
    }

    public MsgPackWriter writeString(DirectBuffer buff, int offset, int length)
    {
        writeStringHeader(length);
        writeRaw(buff, offset, length);
        return this;
    }

    public MsgPackWriter writeLong(long v)
    {
        if (v < -(1L << 5))
        {
            if (v < -(1L << 15))
            {
                if (v < -(1L << 31))
                {
                    buffer.putByte(offset, INT64);
                    ++offset;
                    buffer.putLong(offset, v, BYTE_ORDER);
                    offset += SIZE_OF_LONG;
                }
                else
                {
                    buffer.putByte(offset, INT32);
                    ++offset;
                    buffer.putInt(offset, (int) v, BYTE_ORDER);
                    offset += SIZE_OF_INT;
                }
            }
            else
            {
                if (v < -(1 << 7))
                {
                    buffer.putByte(offset, INT16);
                    ++offset;
                    buffer.putShort(offset, (short) v, BYTE_ORDER);
                    offset += SIZE_OF_SHORT;
                }
                else
                {
                    buffer.putByte(offset, INT8);
                    ++offset;
                    buffer.putByte(offset, (byte) v);
                    ++offset;
                }
            }
        }
        else if (v < (1 << 7))
        {
            buffer.putByte(0, (byte) v);
            ++offset;
        }
        else
        {
            if (v < (1L << 16))
            {
                if (v < (1 << 8))
                {
                    buffer.putByte(offset, UINT8);
                    ++offset;
                    buffer.putByte(offset, (byte) v);
                    ++offset;
                }
                else
                {
                    buffer.putByte(offset, UINT16);
                    ++offset;
                    buffer.putShort(offset, (short) v, BYTE_ORDER);
                    offset += SIZE_OF_SHORT;
                }
            }
            else
            {
                if (v < (1L << 32))
                {
                    buffer.putByte(offset, UINT32);
                    ++offset;
                    buffer.putInt(offset, (int) v, BYTE_ORDER);
                    offset += SIZE_OF_INT;
                }
                else
                {
                    buffer.putByte(offset, UINT64);
                    ++offset;
                    buffer.putLong(offset, v, BYTE_ORDER);
                    offset += SIZE_OF_LONG;
                }
            }
        }
        return this;
    }

    public MsgPackWriter writeStringHeader(int len)
    {
        if (len < (1 << 5))
        {
            buffer.putByte(offset, (byte) (FIXSTR_PREFIX | len));
            ++offset;
        }
        else if (len < (1 << 8))
        {
            buffer.putByte(offset, STR8);
            ++offset;

            buffer.putByte(offset, (byte) len);
            ++offset;
        }
        else if (len < (1 << 16))
        {
            buffer.putByte(offset, STR16);
            ++offset;

            buffer.putShort(offset, (short) len, BYTE_ORDER);
            offset += SIZE_OF_SHORT;
        }
        else
        {
            buffer.putByte(offset, STR32);
            ++offset;

            buffer.putInt(offset, len, BYTE_ORDER);
            offset += SIZE_OF_INT;
        }

        return this;
    }

    public MsgPackWriter writeBinary(DirectBuffer data)
    {
        return writeBinary(data, 0, data.capacity());
    }

    public MsgPackWriter writeBinary(DirectBuffer data, int offset, int length)
    {
        writeBinaryHeader(length);
        writeRaw(data, offset, length);
        return this;
    }

    public MsgPackWriter writeBinaryHeader(int len)
    {
        if (len < (1 << 8))
        {
            buffer.putByte(offset, BIN8);
            ++offset;

            buffer.putByte(offset, (byte) len);
            ++offset;
        }
        else if (len < (1 << 16))
        {
            buffer.putByte(offset, BIN16);
            ++offset;

            buffer.putShort(offset, (short) len, BYTE_ORDER);
            offset += SIZE_OF_SHORT;
        }
        else
        {
            buffer.putByte(offset, BIN32);
            ++offset;

            buffer.putInt(offset, len, BYTE_ORDER);
            offset += SIZE_OF_INT;
        }

        return this;
    }


    public MsgPackWriter writeBoolean(boolean val)
    {
        buffer.putByte(offset, val ? TRUE : FALSE);
        ++offset;

        return this;
    }

    public MsgPackWriter writeNil()
    {
        buffer.putByte(offset, NIL);
        ++offset;

        return this;
    }

    public MsgPackWriter writeDouble(double value)
    {

        final float floatValue = (float) value;

        if ((double) floatValue == value)
        {
            buffer.putByte(offset, FLOAT32);
            ++offset;

            buffer.putFloat(offset, floatValue, BYTE_ORDER);
            offset += SIZE_OF_FLOAT;
        }
        else
        {
            buffer.putByte(offset, FLOAT64);
            ++offset;

            buffer.putDouble(offset, value, BYTE_ORDER);
            offset += SIZE_OF_DOUBLE;
        }

        return this;
    }

    public int getOffset()
    {
        return offset;
    }

    public static int getEncodedMapHeaderLenght(int size)
    {
        int length = 1;

        if (size >= (1 << 4) && size < (1 << 16))
        {
            length += SIZE_OF_SHORT;
        }
        else
        {
            length += SIZE_OF_INT;
        }

        return length;
    }

    public static int getEncodedStringHeaderLength(int len)
    {
        int encodedLength = 1;

        if (len >= (1 << 5) && len < (1 << 8))
        {
            encodedLength++;
        }
        else if (len < (1 << 16))
        {
            encodedLength += SIZE_OF_SHORT;
        }
        else
        {
            encodedLength += SIZE_OF_INT;
        }

        return encodedLength;
    }

    public static int getEncodedStringLength(int len)
    {
        return getEncodedStringHeaderLength(len) + len;
    }

    public static int getEncodedLongValueLength(long v)
    {
        int length = 1;

        if (v < -(1L << 5))
        {
            if (v < -(1L << 15))
            {
                if (v < -(1L << 31))
                {
                    length += SIZE_OF_LONG;
                }
                else
                {
                    length += SIZE_OF_INT;
                }
            }
            else
            {
                if (v < -(1 << 7))
                {
                    length += SIZE_OF_SHORT;
                }
                else
                {
                    length += SIZE_OF_BYTE;
                }
            }
        }
        else if (v >= (1 << 7))
        {
            if (v < (1L << 16))
            {
                if (v < (1 << 8))
                {
                    length += SIZE_OF_BYTE;
                }
                else
                {
                    length += SIZE_OF_SHORT;
                }
            }
            else
            {
                if (v < (1L << 32))
                {
                    length += SIZE_OF_INT;
                }
                else
                {
                    length += SIZE_OF_LONG;
                }
            }
        }

        return length;
    }

    public static int getEncodedBooleanValueLength()
    {
        return 1;
    }

    public static int getEncodedBinaryValueLength(int len)
    {
        int encodedLength = 1;

        if (len < (1 << 8))
        {
            ++encodedLength;
        }
        else if (len < (1 << 16))
        {
            encodedLength += SIZE_OF_SHORT;
        }
        else
        {
            encodedLength += SIZE_OF_INT;
        }

        return encodedLength;
    }
}
