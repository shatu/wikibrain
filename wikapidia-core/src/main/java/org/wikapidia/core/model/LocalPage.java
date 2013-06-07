package org.wikapidia.core.model;

import org.wikapidia.core.WikapidiaException;
import org.wikapidia.core.lang.Language;

/**
 */
public class LocalPage {

    protected Language language;
    protected int localId;
    protected Title title;

    protected LocalPage(Language language, int localId, Title title){
        this.language = language;
        this.localId = localId;
        this.title = title;
    }

    public int getLocalId() {
        return localId;
    }

    public Title getTitle() {
        return title;
    }

    public Language getLanguage() {
        return language;
    }

    public LocalConceptType getLocalConceptType(){
        if (this instanceof LocalArticle){
            return LocalConceptType.ARTICLE;
        }else if (this instanceof LocalCategory){
            return LocalConceptType.CATEGORY;
        }else{
            return LocalConceptType.OTHER;
        }
    }

    public int hashCode(){
        return (language.getId() + "_" + localId).hashCode(); //non-optimal
    }

    public boolean equals(Object o){
        if (this.getClass().equals(o.getClass())){
            LocalPage input = (LocalPage)o;
            return (input.getLanguage().equals(this.getLanguage()) && input.getLocalId() ==
                    this.getLocalId());
        }else{
            return false;
        }
    }

    protected void unsupported() throws WikapidiaException {
        throw new WikapidiaException("This local concept was generated using a query " +
                "source that does not support this field in LocalPage");
    }

    public enum LocalConceptType{
        ARTICLE, CATEGORY, OTHER
    }
}