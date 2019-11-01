package cn.acgq.model;

import java.time.Instant;

public class News {
    private int id;


    private String title;
    private String src;
    private String body;
    private Instant createDate;
    private Instant modifyDate;

    public News(News old) {
        this.createDate = createDate;
        this.modifyDate = modifyDate;
    }

    public News() {
    }

    @Override
    public String toString() {
        return "News{" +
                "title='" + title + '\'' +
                ", src='" + src + '\'' +
                ", body='" + body + '\'' +
                ", createDate=" + createDate +
                ", modifyDate=" + modifyDate +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Instant getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Instant createDate) {
        this.createDate = createDate;
    }

    public Instant getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Instant modifyDate) {
        this.modifyDate = modifyDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }


}
