package com.easylive.entity.query;


/**
 * @description: 查询对象
 * @date: 2025-03-03
 */
public class CategoryInfoQuery extends BaseQuery {
    /**
     * 分类id
     */
    private Integer categoryId;

    /**
     * 分类编码
     */
    private String categoryCode;

    private String categoryCodeFuzzy;

    /**
     * 分类名称
     */
    private String categoryName;

    private String categoryNameFuzzy;

    /**
     * 父级分类id
     */
    private Integer pCategoryId;

    /**
     * 图标
     */
    private String icon;

    private String iconFuzzy;

    /**
     * 背景图
     */
    private String background;

    private String backgroundFuzzy;

    /**
     * 排序字段
     */
    private Integer sort;

    private Integer pCategoryIdOrCategoryId;

    private Boolean convert2Tree;

    public Boolean getConvert2Tree() {
        return convert2Tree;
    }

    public void setConvert2Tree(Boolean convert2Tree) {
        this.convert2Tree = convert2Tree;
    }

    public Integer getpCategoryIdOrCategoryId() {
        return pCategoryIdOrCategoryId;
    }

    public void setpCategoryIdOrCategoryId(Integer pCategoryIdOrCategoryId) {
        this.pCategoryIdOrCategoryId = pCategoryIdOrCategoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setPCategoryId(Integer pCategoryId) {
        this.pCategoryId = pCategoryId;
    }

    public Integer getPCategoryId() {
        return pCategoryId;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getBackground() {
        return background;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getSort() {
        return sort;
    }

    public void setCategoryCodeFuzzy(String categoryCodeFuzzy) {
        this.categoryCodeFuzzy = categoryCodeFuzzy;
    }

    public String getCategoryCodeFuzzy() {
        return categoryCodeFuzzy;
    }

    public void setCategoryNameFuzzy(String categoryNameFuzzy) {
        this.categoryNameFuzzy = categoryNameFuzzy;
    }

    public String getCategoryNameFuzzy() {
        return categoryNameFuzzy;
    }

    public void setIconFuzzy(String iconFuzzy) {
        this.iconFuzzy = iconFuzzy;
    }

    public String getIconFuzzy() {
        return iconFuzzy;
    }

    public void setBackgroundFuzzy(String backgroundFuzzy) {
        this.backgroundFuzzy = backgroundFuzzy;
    }

    public String getBackgroundFuzzy() {
        return backgroundFuzzy;
    }

}