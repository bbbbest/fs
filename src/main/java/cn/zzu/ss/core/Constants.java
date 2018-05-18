package cn.zzu.ss.core;

class Constants {
    /*--------------------Flag Control--------------------*/
    /**
     * set this if Null object reference.
     */
    final static byte FC_NULL = 0x49;

    /**
     * set this if Null object reference.
     */
    final static byte FC_NON_NULL = 0x50;
    /**
     * set this if an object already written into the stream.
     */
    final static byte FC_REFERENCE = 0x51;
    /**
     * set this if a new Object will be wrote.
     */
    final static byte FC_OBJECT = 0x52;
    /**
     * set this if a class name will be wrote.
     */
    final static byte FC_CLASS = 0x53;
    /**
     * set this if one object has been serialized when all fields have been wrote.
     */
    final static byte FC_RESET = 0x54;
    /**
     * set this if start to serialize super value
     */
    final static byte FC_SUPER = 0x55;
    final static byte FC_SUPER_INFO = 0x56;

    /*------------------collection control------*/
    /**
     * set this if a object has been serialized.
     */
    final static byte FC_OBJECT_END = 0x56;

    /* -----------------map control-------------*/
    /**
     * collection control - last element type
     */
    final static byte FC_LAST_ELE_TYPE = 0x57;
    /**
     * last key type
     */
    final static byte FC_LAST_KEY_TYPE = 0x58;
    /**
     * last value type
     */
    final static byte FC_LAST_VALUE_TYPE = 0x59;
    /*--------------------Flag Signature--------------------*/
    final static byte FS_PRIMITIVE = 0x5A;
    final static byte FS_REF_PRIMITIVE = 0x5B;
    final static byte FS_ENUM = 0x5C;
    final static byte FS_COLLECTION = 0x5D;
    final static byte FS_MAP = 0x5E;
    final static byte FS_ARRAY = 0x5F;
    final static byte FS_STRING = 0x60;
    final static byte FS_CLASS = 0x61;
}
