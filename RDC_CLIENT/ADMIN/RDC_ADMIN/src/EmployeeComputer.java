public class EmployeeComputer {
    private String compID;
    private String employeeID;
    private String employeeName;
    private String mail;
    private String compress;

    public EmployeeComputer() {
    }

    public EmployeeComputer(String compID, String employeeID, String employeeName, String mail, String compress) {
        this.compID = compID;
        this.employeeID = employeeID;
        this.employeeName = employeeName;
        this.mail = mail;
        this.compress = compress;
    }

    public String getCompID() {
        return compID;
    }

    public void setCompID(String compID) {
        this.compID = compID;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getCompress() {
        return compress;
    }

    public void setCompress(String compress) {
        this.compress = compress;
    }
}
