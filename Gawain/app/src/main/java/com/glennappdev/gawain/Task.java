package com.glennappdev.gawain;

import java.util.Date;

public class Task {
    private String title, note, subject;
    private boolean isDue, isDone;
    private Date dueDate;

    public Task(){}

    public Task(String title, String note, String subject, Date dueDate, boolean isDue, boolean isDone){
        this.title = title;
        this.note = note;
        this.subject = subject;
        this.dueDate = dueDate;
        this.isDue = isDue;
        this.isDone = isDone;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }


    public boolean isDue() {
        return isDue;
    }

    public void setDue(boolean due) {
        isDue = due;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
