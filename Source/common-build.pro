
-libraryjars <java.home>/lib/rt.jar
-libraryjars <java.home>/lib/jsse.jar
-libraryjars <java.home>/lib/jce.jar

-keepattributes !StackMapTable,InnerClasses,LineNumberTable,Signature,*Annotation*,EnclosingMethod

-keepnames class * implements java.io.Serializable

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepclassmembers class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

