package by.sadovnick.service.enums;

/**
 * Хранит идентификаторы ресурсов, которые будем использовать при генерации ссылки.
 */
public enum LinkType {
    GET_DOC("file/get-doc"),
    GET_PHOTO("file/get-photo");

    private final String link;

    LinkType(String link) {
        this.link = link;
    }

    /**
     * Важно переопределить, чтобы возвращал не имя инама, а часть ссылки ("file/get-doc").
     */
    @Override
    public String toString() {
        return link;
    }
}
