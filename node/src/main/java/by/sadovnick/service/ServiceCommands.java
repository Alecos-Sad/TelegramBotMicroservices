package by.sadovnick.service;

/**
 * Все доступные сервисные команды.
 */
public enum ServiceCommands {
    HELP("/help"),
    REGISTRATION("/registration"),
    CANSEL("/cansel"),
    START("/start");

    private final String cmd;

    ServiceCommands(String cmd) {
        this.cmd = cmd;
    }

    /**
     * Сравнение двух команд
     * @param cmd - команды
     * @return - равны или нет
     */


    public boolean equals(String cmd){
        return this.toString().equals(cmd);
    }

    @Override
    public String toString() {
        return cmd;
    }
}
