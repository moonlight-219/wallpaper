package com.wallpaper.server.common;

public class Constants {
    private Constants() {
    }

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    public static final int DEFAULT_IMAGE_WIDTH = 1920;
    public static final int DEFAULT_IMAGE_HEIGHT = 1080;

    public static final int THUMBNAIL_WIDTH = 300;
    public static final int THUMBNAIL_HEIGHT = 300;

    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_REJECTED = "rejected";

    /** 作品/壁纸审核状态（与数据库 status 整型一致）：0待审 1通过 2拒绝 */
    public static final int AUDIT_PENDING = 0;
    public static final int AUDIT_APPROVED = 1;
    public static final int AUDIT_REJECTED = 2;

    public static final String ACTION_LIKE = "like";
    public static final String ACTION_COLLECT = "collect";
    public static final String ACTION_DOWNLOAD = "download";

    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_USER = "user";

    public static final String TYPE_PHONE = "phone";
    public static final String TYPE_TABLET = "tablet";
    public static final String TYPE_AVATAR = "avatar";
}
