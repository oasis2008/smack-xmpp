/**
* $RCSfile$
* $Revision$
* $Date$
*
* Copyright (C) 2002-2003 Jive Software. All rights reserved.
* ====================================================================
* The Jive Software License (based on Apache Software License, Version 1.1)
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by
*        Jive Software (http://www.jivesoftware.com)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Smack" and "Jive Software" must not be used to
*    endorse or promote products derived from this software without
*    prior written permission. For written permission, please
*    contact webmaster@jivesoftware.com.
*
* 5. Products derived from this software may not be called "Smack",
*    nor may "Smack" appear in their name, without prior written
*    permission of Jive Software.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL JIVE SOFTWARE OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*/

package org.jivesoftware.smackx;

import java.util.*;

import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smackx.packet.DataForm;

/**
 * Represents a Form for gathering data. The form could be of the following types:
 * <ul>
 *  <li>form -> Indicates a form to fill out.</li>
 *  <li>submit -> The form is filled out, and this is the data that is being returned from 
 * the form.</li>
 *  <li>cancel -> The form was cancelled. Tell the asker that piece of information.</li>
 *  <li>result -> Data results being returned from a search, or some other query.</li>
 * </ul>
 * 
 * Depending of the form's type different operations are available. For example, it's only possible
 * to set answers if the form is of type "submit".
 * 
 * @author Gaston Dombiak
 */
public class Form {
    
    public static final String TYPE_FORM = "form";
    public static final String TYPE_SUBMIT = "submit";
    public static final String TYPE_CANCEL = "cancel";
    public static final String TYPE_RESULT = "result";
    
    private DataForm dataForm;
    
    /**
     * Returns a new ReportedData if the packet is used for gathering data and includes an 
     * extension that matches the elementName and namespace "x","jabber:x:data".  
     * 
     * @param packet the packet used for gathering data.
     */
    public static Form getFormFrom(Packet packet) {
        // Check if the packet includes the DataForm extension
        PacketExtension packetExtension = packet.getExtension("x","jabber:x:data");
        if (packetExtension != null) {
            // Check if the existing DataForm is not a result of a search
            DataForm dataForm = (DataForm) packetExtension;
            if (dataForm.getReportedData() == null)
                return new Form(dataForm);
        }
        // Otherwise return null
        return null;
    }

    /**
     * Creates a new Form that will wrap an existing DataForm. The wrapped DataForm must be
     * used for gathering data. 
     * 
     * @param dataForm the data form used for gathering data. 
     */
    private Form(DataForm dataForm) {
        this.dataForm = dataForm;
    }
    
    /**
     * Creates a new Form of a given type from scratch.<p>
     *  
     * Possible form types are:
     * <ul>
     *  <li>form -> Indicates a form to fill out.</li>
     *  <li>submit -> The form is filled out, and this is the data that is being returned from 
     * the form.</li>
     *  <li>cancel -> The form was cancelled. Tell the asker that piece of information.</li>
     *  <li>result -> Data results being returned from a search, or some other query.</li>
     * </ul>
     * 
     * @param type the form's type (e.g. form, submit,cancel,result).
     */
    public Form(String type) {
        this.dataForm = new DataForm(type);
    }
    
    /**
     * Adds a new field to complete as part of the form.
     * 
     * @param field the field to complete.
     */
    public void addField(FormField field) {
        dataForm.addField(field);
    }
    
    /**
     * Sets a new answer as part of a form's field. The field whose variable matches the requested 
     * variable will be completed with the specified value. If no field could be found for 
     * the specified variable then an exception will be raised.
     * 
     * @param variable the variable that was completed.
     * @param value the value that was answered.
     * @throws IllegalStateException if the form is not of type "submit".
     * @throws IllegalArgumentException if the form does not include the specified variable.
     */
    public void setAnswer(String variable, String value) {
        if (!isSubmitType()) {
            throw new IllegalStateException("Cannot set an answer if the form is not of type " +
            "\"submit\"");
        }
        FormField field = getField(variable);
        if (field != null) {
            field.resetValues();
            field.addValue(value);
        }
        else {
            throw new IllegalArgumentException("Couldn't find a field for the specified variable.");
        }
    }

    /**
     * Sets new answers as part of a form's field. The field whose variable matches the requested 
     * variable will be completed with the specified values. If no field could be found for 
     * the specified variable then an exception will be raised.
     * 
     * @param variable the variable that was completed.
     * @param values the values that were answered.
     * @throws IllegalStateException if the form is not of type "submit".
     * @throws IllegalArgumentException if the form does not include the specified variable.
     */
    public void setAnswer(String variable, List values) {
        if (!isSubmitType()) {
            throw new IllegalStateException("Cannot set an answer if the form is not of type " +
            "\"submit\"");
        }
        FormField field = getField(variable);
        if (field != null) {
            field.resetValues();
            field.addValues(values);
        }
        else {
            throw new IllegalArgumentException("Couldn't find a field for the specified variable.");
        }
    }

    /**
     * Returns an Iterator for the fields that are part of the form.
     *
     * @return an Iterator for the fields that are part of the form.
     */
    public Iterator getFields() {
        return dataForm.getFields();
    }

    /**
     * Returns the field of the form whose variable matches the specified variable.
     * The fields of type FIXED will never be returned since they do not specify a 
     * variable. 
     * 
     * @param variable the variable to look for in the form fields. 
     * @return the field of the form whose variable matches the specified variable.
     */
    public FormField getField(String variable) {
        if (variable == null || variable.equals("")) {
            throw new IllegalArgumentException("Variable must not be null or blank.");
        }
        // Look for the field whose variable matches the requested variable
        FormField field;
        for (Iterator it=getFields();it.hasNext();) {
            field = (FormField)it.next();
            if (variable.equals(field.getVariable())) {
                return field;
            }
        }
        return null;
    }

    /**
     * Returns the instructions that explain how to fill out the form and what the form is about.
     * 
     * @return instructions that explain how to fill out the form.
     */
    public String getInstructions() {
        StringBuffer sb = new StringBuffer();
        // Join the list of instructions together separated by newlines
        for (Iterator it = dataForm.getInstructions(); it.hasNext();) {
            sb.append((String) it.next());
            // If this is not the last instruction then append a newline
            if (it.hasNext()) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }


    /**
     * Returns the description of the data. It is similar to the title on a web page or an X 
     * window.  You can put a <title/> on either a form to fill out, or a set of data results.
     * 
     * @return description of the data.
     */
    public String getTitle() {
        return dataForm.getTitle();
    }


    /**
     * Returns the meaning of the data within the context. The data could be part of a form
     * to fill out, a form submission or data results.<p>
     * 
     * Possible form types are:
     * <ul>
     *  <li>form -> Indicates a form to fill out.</li>
     *  <li>submit -> The form is filled out, and this is the data that is being returned from 
     * the form.</li>
     *  <li>cancel -> The form was cancelled. Tell the asker that piece of information.</li>
     *  <li>result -> Data results being returned from a search, or some other query.</li>
     * </ul>
     * 
     * @return the form's type.
     */
    public String getType() {
        return dataForm.getType(); 
    }
    

    /**
     * Sets instructions that explain how to fill out the form and what the form is about.
     * 
     * @param instructions instructions that explain how to fill out the form.
     */
    public void setInstructions(String instructions) {
        // Split the instructions into multiple instructions for each existent newline
        ArrayList instructionsList = new ArrayList();
        StringTokenizer st = new StringTokenizer(instructions, "\n");
        while (st.hasMoreTokens()) {
            instructionsList.add(st.nextToken());
        }
        // Set the new list of instructions
        dataForm.setInstructions(instructionsList);
        
    }


    /**
     * Sets the description of the data. It is similar to the title on a web page or an X window.
     * You can put a <title/> on either a form to fill out, or a set of data results.
     * 
     * @param title description of the data.
     */
    public void setTitle(String title) {
        dataForm.setTitle(title);
    }
    
    /**
     * Returns a DataForm that serves to send this Form to the server. If the form is of type 
     * submit, it may contain fields with no value. These fields will be removed since they only 
     * exist to assist the user while editing/completing the form in a UI. 
     * 
     * @return the wrapped DataForm.
     */
    public DataForm getDataFormToSend() {
        if (isSubmitType()) {
            // Create a new DataForm that contains only the answered fields 
            DataForm dataFormToSend = new DataForm(getType());
            for(Iterator it=getFields();it.hasNext();) {
                FormField field = (FormField)it.next();
                if (field.getValues().hasNext()) {
                    dataFormToSend.addField(field);
                }
            }
            return dataFormToSend;
        }
        return dataForm;
    }
    
    /**
     * Returns true if the form is a form to fill out.
     * 
     * @return if the form is a form to fill out.
     */
    private boolean isFormType() {
        return TYPE_FORM.equals(dataForm.getType());
    }
    
    /**
     * Returns true if the form is a form to submit.
     * 
     * @return if the form is a form to submit.
     */
    private boolean isSubmitType() {
        return TYPE_SUBMIT.equals(dataForm.getType());
    }

    /**
     * Returns a new Form to submit the completed values. The new Form will include all the fields
     * of the original form except for the fields of type FIXED. Only the HIDDEN fields will 
     * include the same value of the original form. The other fields of the new form MUST be 
     * completed. If a field remains with no answer when sending the completed form, then it won't 
     * be included as part of the completed form.<p>
     * 
     * The reason why the fields with variables are included in the new form is to provide a model 
     * for binding with any UI. This means that the UIs will use the original form (of type 
     * "form") to learn how to render the form, but the UIs will bind the fields to the form of
     * type submit.
     * 
     * @return a Form to submit the completed values.
     */
    public Form createAnswerForm() {
        if (!isFormType()) {
            throw new IllegalStateException("Only forms of type \"form\" could be answered");
        }
        // Create a new Form
        Form form = new Form(TYPE_SUBMIT);
        for (Iterator fields=getFields(); fields.hasNext();) {
            FormField field = (FormField)fields.next();
            // Add to the new form any type of field that includes a variable.
            // Note: The fields of type FIXED are the only ones that don't specify a variable
            if (field.getVariable() != null) {
                form.addField(new FormField(field.getVariable()));
                // Set the answer ONLY to the hidden fields 
                if (FormField.TYPE_HIDDEN.equals(field.getType())) {
                    // Since a hidden field could have many values we need to collect them 
                    // in a list
                    List values = new ArrayList();
                    for (Iterator it=field.getValues();it.hasNext();) {
                        values.add((String)it.next());
                    }
                    form.setAnswer(field.getVariable(), values);
                }                
            }
        }
        return form;
    }

}
