/* Copyright 2016 Luzhuo. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.luzhuo.numberseparateedittext;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

/**
 * =================================================
 * <p>
 * Author: Luzhuo
 * <p>
 * Version: 1.0
 * <p>
 * Creation Date: 2016/5/17 14:07
 * <p>
 * Description:<pre>
 * Number input separate widget:
 *     NumberType.Expand : expand
 *     NumberType.Phone : eleven phone number 3-4-4
 *     NumberType.BankCard : sixteen or nineteen card number 4-4-4-4-3
 *     NumberType.IdCard : Eighteen idcard number 6-8-4 </pre>
 * <p>
 * Revision History:
 * <p>
 * Copyright: Copyright 2016 Luzhuo. All rights reserved.
 * <p>
 * =================================================
 **/
public class NumberSeparateEditText extends EditText {
    private NumberType numberType;
    private static final String TAG = NumberSeparateEditText.class.getSimpleName();

    private String AUTO = "http://schemas.android.com/apk/res-auto";
    private String[] numberTypeAttrs = new String[]{"0", "1", "2", "3", "4"};
    private final int PHONEMAXLENGTH = 11, BANKCARDMAXLENGTH = 19, IDCARDMAXLENGTH = 18;
    private int ExpandMaxLength = Integer.MAX_VALUE; private ExpandParameter expandParameter;

    public enum NumberType{
        Phone, BankCard, IdCard, Expand
    }

    public NumberSeparateEditText(Context context) {
        super(context);
        initData();
    }

    public NumberSeparateEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        if(numberTypeAttrs[0].equals(attrs.getAttributeValue(AUTO, "NumberType"))) setNumberType(NumberType.Expand);
        if(numberTypeAttrs[1].equals(attrs.getAttributeValue(AUTO, "NumberType"))) setNumberType(NumberType.Phone);
        if(numberTypeAttrs[2].equals(attrs.getAttributeValue(AUTO, "NumberType"))) setNumberType(NumberType.BankCard);
        if(numberTypeAttrs[3].equals(attrs.getAttributeValue(AUTO, "NumberType"))) setNumberType(NumberType.IdCard);

        initData();
    }

    public NumberSeparateEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initData();
    }

    private void initData() {
        this.addTextChangedListener(watcher);
    }

    /**
     * set number type.
     * @param numtype NumberType(Phone, BankCard)
     */
    public void setNumberType(NumberType numtype){
        this.numberType = numtype;

        switch (numtype){
            case IdCard: this.setInputType(InputType.TYPE_CLASS_PHONE); break;
            default: this.setInputType(InputType.TYPE_CLASS_NUMBER); break;
        }
    }

    private boolean isRun = false;
    TextWatcher watcher = new TextWatcherImpl() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Perform only user input data, callback setText () method to filter.
            if(isRun){
                isRun = false;
                return;
            }
            isRun = true;

            // Log.e(TAG, "CharSequence"+s.toString());

            // data processing
            String separateText = separateNumberText(getNumberText(s.toString()));
            NumberSeparateEditText.this.setText(separateText);
            NumberSeparateEditText.this.setSelection(separateText.length());
            if(textWatcher != null) textWatcher.onTextChanged(getNumber());
        }
    };

    /**
     * separate number.
     * @param originalData user input text number.
     * @return data separate.
     */
    private String separateNumberText(String originalData) {
        String data;
        // avoid pasting data.
        if(numberType == NumberType.Phone) {
            data = originalData.substring(0, originalData.length() <= PHONEMAXLENGTH ? originalData.length() : PHONEMAXLENGTH);
        }else if(numberType == NumberType.BankCard){
            data = originalData.substring(0, originalData.length() <= BANKCARDMAXLENGTH ? originalData.length() : BANKCARDMAXLENGTH);
        }else if(numberType == NumberType.IdCard){
            data = originalData.substring(0, originalData.length() <= IDCARDMAXLENGTH ? originalData.length() : IDCARDMAXLENGTH);
        }else if(numberType == NumberType.Expand){
            data = originalData.substring(0, originalData.length() <= ExpandMaxLength ? originalData.length() : ExpandMaxLength);
        } else {
            data = originalData;
        }

        char[] bytes = data.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (int x = 0; x < bytes.length; x++){
            if(numberType != null) {
                switch (numberType){
                    case Expand:
                        if(expandParameter != null){
                            if(expandParameter.matching(x)) sbAppend(true, sb, bytes, x);
                            else sbAppend(false, sb, bytes, x);
                        }else sbAppend(false, sb, bytes, x);
                        break;
                    case Phone:
                        if (x == 3 || x == 7) sbAppend(true, sb, bytes, x);
                        else sbAppend(false, sb, bytes, x);
                        break;
                    case BankCard:
                        if (x == 4 || x == 8 || x == 12 || x == 16) sbAppend(true, sb, bytes, x);
                        else sbAppend(false, sb, bytes, x);
                        break;
                    case IdCard:
                        if (x == 6 || x == 14) sbAppend(true, sb, bytes, x);
                        else sbAppend(false, sb, bytes, x);
                        break;
                }
            }else{
                sbAppend(false, sb, bytes, x);
            }
        }
        return sb.toString();
    }

    private void sbAppend(boolean addSpace,StringBuffer sb, char[] bytes, int x){
        if(addSpace) sb.append(" ");
        sb.append(bytes[x]);
    }

    /**
     * get number string.
     * @return number.
     */
    public String getNumber() {
        return getNumberText(this.getText().toString());
    }

    /**
     * trim data.
     * @param data need to trim data
     * @return finishing complete data.
     */
    private String getNumberText(String data){
        String text = data.trim();
        char[] bytes = text.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (int x = 0; x < bytes.length; x++){
            if(TextUtils.isEmpty(String.valueOf(bytes[x]).trim())) continue;
            else{
                switch (numberType){
                    case IdCard: // 1234567890 valid, any other characters are X.
                        if(bytes[x] >= 48 && bytes[x] <= 57) sb.append(bytes[x]);
                        else sb.append('X'); break;
                    default:
                        sb.append(bytes[x]); break;
                }
            }
        }
        return sb.toString();
    }

    private NumberTextWatcher textWatcher;
    /**
     * set edittext change listener.
     * @param textWatcher NumberTextWatcher interface.
     */
    public void addTextChangedListener(NumberTextWatcher textWatcher){
        this.textWatcher = textWatcher;
    }

    /**
     * set expand
     * @param numbermax Limit the length.
     * @param expandParameter If it is null, don't break up, or implement ExpandParameter interface
     */
    public void setExpand(int numbermax, ExpandParameter expandParameter){
        this.ExpandMaxLength = numbermax;
        this.expandParameter = expandParameter;
    }
}
