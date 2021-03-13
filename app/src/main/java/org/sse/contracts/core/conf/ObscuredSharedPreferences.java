package org.sse.contracts.core.conf;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Base64;

import org.sse.contracts.Utils;

import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;


public class ObscuredSharedPreferences implements SharedPreferences
{
    protected static final String UTF8 = "utf-8";
    private static final char[] SEKRIT = {'1', '1', '1', '1', '1', '1', '1', '1', '1'};
    ; // INSERT A RANDOM PASSWORD HERE.
    // Don't use anything you wouldn't want to
    // get out there if someone decompiled
    // your app.

    protected SharedPreferences delegate;
    protected Context context;

    public ObscuredSharedPreferences(Context context, SharedPreferences delegate)
    {
        this.delegate = delegate;
        this.context = context;
    }

    public Editor edit()
    {
        return new Editor();
    }

    @Override
    public Map<String, ?> getAll()
    {
        throw new UnsupportedOperationException(); // left as an exercise to the reader
    }

    @Override
    public boolean getBoolean(String key, boolean defValue)
    {
        try
        {
            final String v = delegate.getString(key, null);
            if (v == null)
            {
                edit().putBoolean(key, defValue).commit();
                return defValue;
            } else
                return Boolean.parseBoolean(decrypt(v));
        } catch (Exception e)
        {
            Utils.exceptionReport(e);
            // TODO: Возможно, в этом случае нужно не выставлять значение по умолчанию, т.к. сломав запись, можно вернуть демо режим.
            edit().putBoolean(key, defValue).commit();
            return defValue;
        }
    }

    @Override
    public float getFloat(String key, float defValue)
    {
        try
        {
            final String v = delegate.getString(key, null);
            if (v == null)
            {
                edit().putFloat(key, defValue).commit();
                return defValue;
            } else
                return Float.parseFloat(decrypt(v));
        } catch (Exception e)
        {
            Utils.exceptionReport(e);
            // TODO: Возможно, в этом случае нужно не выставлять значение по умолчанию, т.к. сломав запись, можно вернуть демо режим.
            edit().putFloat(key, defValue).commit();
            return defValue;
        }
    }

    @Override
    public int getInt(String key, int defValue)
    {
        try
        {
            final String v = delegate.getString(key, null);
            if (v == null)
            {
                edit().putInt(key, defValue).commit();
                return defValue;
            } else
                return Integer.parseInt(decrypt(v));
        } catch (Exception e)
        {
            Utils.exceptionReport(e);
            // TODO: Возможно, в этом случае нужно не выставлять значение по умолчанию, т.к. сломав запись, можно вернуть демо режим.
            edit().putInt(key, defValue).commit();
            return defValue;
        }
    }

    @Override
    public long getLong(String key, long defValue)
    {
        try
        {
            final String v = delegate.getString(key, null);
            if (v == null)
            {
                edit().putLong(key, defValue).commit();
                return defValue;
            } else
                return Long.parseLong(decrypt(v));
        } catch (Exception e)
        {
            Utils.exceptionReport(e);
            // TODO: Возможно, в этом случае нужно не выставлять значение по умолчанию, т.к. сломав запись, можно вернуть демо режим.
            edit().putLong(key, defValue).commit();
            return defValue;
        }
    }

    @Override
    public String getString(String key, String defValue)
    {
        try
        {
            final String v = delegate.getString(key, null);
            if (v == null)
            {
                edit().putString(key, defValue).commit();
                return defValue;
            } else return decrypt(v);
        } catch (Exception e)
        {
            Utils.exceptionReport(e);
            // TODO: Возможно, в этом случае нужно не выставлять значение по умолчанию, т.к. сломав запись, можно вернуть демо режим.
            edit().putString(key, defValue).commit();
            return defValue;
        }
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, Set<String> defValues)
    {
        return null;
    }

    @Override
    public boolean contains(String s)
    {
        return delegate.contains(s);
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener)
    {
        delegate.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener)
    {
        delegate.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    protected String encrypt(String value)
    {

        try
        {
            final byte[] bytes = value != null ? value.getBytes(UTF8) : new byte[0];
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(SEKRIT));
            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID).getBytes(UTF8), 20));
            return new String(Base64.encode(pbeCipher.doFinal(bytes), Base64.NO_WRAP), UTF8);

        } catch (Exception e)
        {
            Utils.exceptionReport(e);
            throw new RuntimeException(e);
        }
    }

    protected String decrypt(String value) throws Exception
    {

        final byte[] bytes = value != null ? Base64.decode(value, Base64.DEFAULT) : new byte[0];
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(SEKRIT));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID).getBytes(UTF8), 20));
        return new String(pbeCipher.doFinal(bytes), UTF8);
    }

    public class Editor implements SharedPreferences.Editor
    {
        protected SharedPreferences.Editor delegate;

        public Editor()
        {
            this.delegate = ObscuredSharedPreferences.this.delegate.edit();
        }

        @Override
        public Editor putBoolean(String key, boolean value)
        {
            delegate.putString(key, encrypt(Boolean.toString(value)));
            return this;
        }

        @Override
        public Editor putFloat(String key, float value)
        {
            delegate.putString(key, encrypt(Float.toString(value)));
            return this;
        }

        @Override
        public Editor putInt(String key, int value)
        {
            delegate.putString(key, encrypt(Integer.toString(value)));
            return this;
        }

        @Override
        public Editor putLong(String key, long value)
        {
            delegate.putString(key, encrypt(Long.toString(value)));
            return this;
        }

        @Override
        public Editor putString(String key, String value)
        {
            delegate.putString(key, encrypt(value));
            return this;
        }

        @Override
        public SharedPreferences.Editor putStringSet(String key, Set<String> values)
        {
            return null;
        }

        @Override
        public void apply()
        {
            delegate.apply();
        }

        @Override
        public Editor clear()
        {
            delegate.clear();
            return this;
        }

        @Override
        public boolean commit()
        {
            return delegate.commit();
        }

        @Override
        public Editor remove(String s)
        {
            delegate.remove(s);
            return this;
        }
    }
}
