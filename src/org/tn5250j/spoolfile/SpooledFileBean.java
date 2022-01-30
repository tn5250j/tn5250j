/**
 *
 */
package org.tn5250j.spoolfile;

import com.ibm.as400.access.PrintObject;
import com.ibm.as400.access.SpooledFile;

import javafx.beans.property.SimpleStringProperty;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SpooledFileBean {
    private final SpooledFile file;

    private final String spoolName;
    private final Integer number;
    private final String jobName;
    private final String jobUser;
    private final String jobNumber;
    private final String queue;
    private final String userData;
    private final String status;
    private final Integer totalPages;
    private final Integer currentPage;
    private final Integer copies;
    private final String formType;
    private final String priority;
    private final String creationDate;
    private final Integer size;

    private final SimpleStringProperty propertySpoolName = new SimpleStringProperty();
    private final SimpleStringProperty propertyNumber = new SimpleStringProperty();
    private final SimpleStringProperty propertyJobName = new SimpleStringProperty();
    private final SimpleStringProperty propertyJobUser = new SimpleStringProperty();
    private final SimpleStringProperty propertyjobNumber = new SimpleStringProperty();
    private final SimpleStringProperty propertyQueue = new SimpleStringProperty();
    private final SimpleStringProperty propertyUserData = new SimpleStringProperty();
    private final SimpleStringProperty propertyStatus = new SimpleStringProperty();
    private final SimpleStringProperty propertyTotalPages = new SimpleStringProperty();
    private final SimpleStringProperty propertyCurrentPage = new SimpleStringProperty();
    private final SimpleStringProperty propertyCopies = new SimpleStringProperty();
    private final SimpleStringProperty propertyFormType = new SimpleStringProperty();
    private final SimpleStringProperty propertyPriority = new SimpleStringProperty();
    private final SimpleStringProperty propertyCreationDate = new SimpleStringProperty();
    private final SimpleStringProperty propertySize = new SimpleStringProperty();

    public SpooledFileBean(final SpooledFile file) {
        this.file = file;

        // Spool Name|100
        this.spoolName = file.getName();
        propertySpoolName.set(spoolName);
        //Spool Number|90
        this.number = loadIntegerAttribute(PrintObject.ATTR_SPLFNUM);
        propertyNumber.set(number == null ? null : number.toString());
        //spoolNumber|100
        this.jobName = loadStringAttribute(PrintObject.ATTR_JOBNAME);
        propertyJobName.set(jobName);
        //Job User|100
        this.jobUser = loadStringAttribute(PrintObject.ATTR_JOBUSER);
        propertyJobUser.set(jobUser);
        //Job Number|90
        this.jobNumber = loadStringAttribute(PrintObject.ATTR_JOBNUMBER);
        propertyjobNumber.set(jobNumber);
        //Queue|200
        this.queue = loadStringAttribute(PrintObject.ATTR_OUTPUT_QUEUE);
        propertyQueue.set(queue);
        //User Data|100
        this.userData = loadStringAttribute(PrintObject.ATTR_USERDATA);
        propertyUserData.set(userData);
        //Status|100
        this.status = loadStringAttribute(PrintObject.ATTR_SPLFSTATUS);
        propertyStatus.set(status);
        //Total Pages|90
        this.totalPages = loadIntegerAttribute(PrintObject.ATTR_PAGES);
        propertyTotalPages.set(totalPages == null ? null : totalPages.toString());
        //Current Page|90
        this.currentPage = loadIntegerAttribute(PrintObject.ATTR_CURPAGE);
        propertyCurrentPage.set(currentPage == null ? null : currentPage.toString());
        //Copies|90
        this.copies = loadIntegerAttribute(PrintObject.ATTR_COPIES);
        propertyCopies.set(copies == null ? null : copies.toString());
        //Form Type|100
        this.formType = loadStringAttribute(PrintObject.ATTR_FORMTYPE);
        propertyFormType.set(formType);
        //Priority|40
        this.priority = loadStringAttribute(PrintObject.ATTR_OUTPTY);
        propertyPriority.set(priority);
        //Creation Date/Time|175
        this.creationDate = loadCreateDateTime();
        propertyCreationDate.set(creationDate);
        //Size|120
        this.size = loadIntegerAttribute(PrintObject.ATTR_NUMBYTES);
        propertySize.set(size == null ? null : size.toString());
    }

    public SpooledFile getFile() {
        return file;
    }

    /**
     * Load a Printer Object string attribute into our row vector
     *
     * @param attribute
     */
    private String loadStringAttribute(final int attribute) {
        try {
            return file.getStringAttribute(attribute);
        } catch (final Exception ex) {
            return null;
            //row.add("Attribute Not supported");
        }
    }

    /**
     * Load a Printer Object integer/numeric attribute into our row vector
     *
     * @param attribute
     */
    private Integer loadIntegerAttribute(final int attribute) {
        try {
            return file.getIntegerAttribute(attribute);
        } catch (final Exception ex) {
//         System.out.println(ex.getMessage());
            return null;
        }
    }

    /**
     * Format the create date and time into a string to be used
     * @param p
     * @param row
     */
    private String loadCreateDateTime() {
        try {
            final String datetime = formatDate(file.getStringAttribute(PrintObject.ATTR_DATE)) +
                    " " +
                    formatTime(file.getStringAttribute(PrintObject.ATTR_TIME));
            return datetime;
        } catch (final Exception ex) {
            return null;
        }
    }

    /**
     * Format the date string from the string passed
     *    format is cyymmdd
     *    c  - century -  0 1900
     *                   1 2000
     *    yy -  year
     *    mm -  month
     *    dd -  day
     *
     * @param dateString String in the format as above
     * @return formatted date string
     */
    static String formatDate(final String dateString) {

        if (dateString != null) {

            final char[] dateArray = dateString.toCharArray();
            // check if the length is correct length for formatting the string should
            //  be in the format cyymmdd where
            //    c = 0 -> 19
            //    c = 1 -> 20
            if (dateArray.length != 7)
                return dateString;

            final StringBuffer db = new StringBuffer(10);

            // this will strip out the starting century char as described above
            db.append(dateArray, 1, 6);

            // now we find out what the century byte was and insert the correct
            //  2 char number century in the buffer.
            if (dateArray[0] == '0')
                db.insert(0, "19");
            else
                db.insert(0, "20");

            db.insert(4, '/'); // add the first date seperator
            db.insert(7, '/'); // add the second date seperator
            return db.toString();
        } else
            return "";

    }

    /**
     * Format the time string with separator of ':'
     *
     * @param timeString
     * @return
     */
    static String formatTime(final String timeString) {

        if (timeString != null) {

            final StringBuffer tb = new StringBuffer(timeString);

            tb.insert(tb.length() - 2, ':');
            tb.insert(tb.length() - 5, ':');
            return tb.toString();
        } else
            return "";
    }

    public String getSpoolName() {
        return spoolName;
    }
    public Integer getNumber() {
        return number;
    }
    public String getJobName() {
        return jobName;
    }
    public String getJobUser() {
        return jobUser;
    }
    public String getJobNumber() {
        return jobNumber;
    }
    public String getQueue() {
        return queue;
    }
    public String getUserData() {
        return userData;
    }
    public String getStatus() {
        return status;
    }
    public Integer getTotalPages() {
        return totalPages;
    }
    public Integer getCurrentPage() {
        return currentPage;
    }
    public Integer getCopies() {
        return copies;
    }
    public String getFormType() {
        return formType;
    }
    public String getPriority() {
        return priority;
    }
    public String getCreationDate() {
        return creationDate;
    }
    public Integer getSize() {
        return size;
    }

    public SimpleStringProperty getPropertySpoolName() {
        return propertySpoolName;
    }
    public SimpleStringProperty getPropertyNumber() {
        return propertyNumber;
    }
    public SimpleStringProperty getPropertyJobName() {
        return propertyJobName;
    }
    public SimpleStringProperty getPropertyJobUser() {
        return propertyJobUser;
    }
    public SimpleStringProperty getPropertyjobNumber() {
        return propertyjobNumber;
    }
    public SimpleStringProperty getPropertyQueue() {
        return propertyQueue;
    }
    public SimpleStringProperty getPropertyUserData() {
        return propertyUserData;
    }
    public SimpleStringProperty getPropertyStatus() {
        return propertyStatus;
    }
    public SimpleStringProperty getPropertyTotalPages() {
        return propertyTotalPages;
    }
    public SimpleStringProperty getPropertyCurrentPage() {
        return propertyCurrentPage;
    }
    public SimpleStringProperty getPropertyCopies() {
        return propertyCopies;
    }
    public SimpleStringProperty getPropertyFormType() {
        return propertyFormType;
    }
    public SimpleStringProperty getPropertyPriority() {
        return propertyPriority;
    }
    public SimpleStringProperty getPropertyCreationDate() {
        return propertyCreationDate;
    }
    public SimpleStringProperty getPropertySize() {
        return propertySize;
    }
}
