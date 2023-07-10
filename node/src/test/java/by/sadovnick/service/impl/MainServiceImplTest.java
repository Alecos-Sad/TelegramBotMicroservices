package by.sadovnick.service.impl;

import by.sadovnick.dao.RowDataDao;
import by.sadovnick.entity.RowData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashSet;
import java.util.Set;

@SpringBootTest
class MainServiceImplTest {
    @Autowired
    private RowDataDao rowDataDao;

    @Test
    public void testSaveRowData() {
        Update update = new Update();
        Message msg = new Message();
        msg.setText("ololo");
        update.setMessage(msg);
        RowData rowData = RowData.builder()
                .event(update)
                .build();
        Set<RowData> testData = new HashSet<>();
        testData.add(rowData);
        rowDataDao.save(rowData);
        //Будет false, если над entity стоит @Data\
        //Будет true, если над entity стоит @Getter,@Setter и @EqualsAndHashCode(exclude = "id")
        Assert.isTrue(testData.contains(rowData), "Entity not found in the set");
    }
}