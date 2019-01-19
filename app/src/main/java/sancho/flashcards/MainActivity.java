package sancho.flashcards;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import static java.lang.Math.random;

public class MainActivity extends Activity implements OnClickListener {

    final String LOG_TAG = "myLogs";

    Button btnAdd, btnBack, btnLearned, btnList,btnShuffle,btnReset;
    Button btnFlip, btnNext, btnFill, btndlt, switchLangBtn;
    TextView txtCard;
    EditText etFront, etBack;
    CheckBox chLock;
    public boolean switchLang = true;
    public boolean flag;    //флаг со значением true or false, при нажатии на кнопку меняет свое значение на противоположное
    //таким образом на одной кнопке записано два разных кода, которые чередуются
    int pos; //текущая позиция курсора
    String cardSideA;
    String cardSideB;

    DBHelper dbHelper; //создаем объект dbHelper класса DBHelper для управления БД. Сам класс описан ниже.

    SQLiteDatabase db;
    Cursor c;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //при открытии, кнопки еще не нажаты
        pos = 0;

        //мои кнопки и текст вью начало
        btnFlip = (Button) findViewById(R.id.btnFlip);
        btnFlip.setOnClickListener(this);


        btnShuffle = (Button) findViewById(R.id.btnShuffle);
        btnShuffle.setOnClickListener(this);

        btnList = (Button) findViewById(R.id.btnList);
        btnList.setOnClickListener(this);

        btnLearned = (Button) findViewById(R.id.btnLearned);
        btnLearned.setOnClickListener(this);

        switchLangBtn = (Button) findViewById(R.id.switchLangBtn);
        chLock = (CheckBox) findViewById(R.id.chLock);

        btnFill = (Button) findViewById(R.id.btnFill);
        btnFill.setOnClickListener(this);

        btndlt = (Button) findViewById(R.id.btndlt);
        btndlt.setOnClickListener(this);

        btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);

        txtCard = (TextView) findViewById(R.id.txtCard);
        //мои кнопки и текст вью конец

        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);

        btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);

        btnReset = (Button) findViewById(R.id.btnReset);
        btnReset.setOnClickListener(this);

        etFront = (EditText) findViewById(R.id.etFront);
        etBack = (EditText) findViewById(R.id.etBack);

        // создаем объект для создания и управления версиями БД
        dbHelper = new DBHelper(this);

        //дважды нажимаем на чек бокс, чтобы выключить кнопки удаления и заполнения базы
        //можно было изящнее написать, но так было быстрее
        chLock.performClick();
        chLock.performClick();

//        txtCard.setText("нажмите Next чтобы начать");
        try {
            db = dbHelper.getWritableDatabase();
//        ReCreateTable();
            GetCursorNewWords();
//        db = dbHelper.getWritableDatabase();
//        c = db.query("mytable", null, null, null, null, null,null);
            c.moveToFirst();
            btnNext.performClick(); //чтобы установить первое слово нажимаем NEXT
            //таким образом показ начинается со второго слова из базы
        }catch (Exception e){
            txtCard.setText("нет слов для изучения");
        }

//   NewTry();
    }

    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            // конструктор суперкласса
            super(context, "myDB", null, 1);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            Log.d(LOG_TAG, "--- onCreate database ---");
            //удаляем таблицу
//            db.execSQL("drop table mytable;");
//            db.execSQL("DROP TABLE IF EXISTS " + "mytable");
            // создаем таблицу с полями
            db.execSQL("create table mytable ("
                    + "_id integer primary key autoincrement,"
                    + "front text,"
                    + "back text"
                    + "learned"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            /**Если номер версии базы данных в приложении отличается от версии
             * базы данных на устройстве, то запускается этот метод onUpgrade
             */
            Log.d(LOG_TAG, "--- onUpgrade database ---");
        }
    }

    public void onClick(View v) {

        ContentValues cv = new ContentValues(); //создаем объект для даннных

        if (switchLang) {
            cardSideA = "front";
            cardSideB = "back";
        } else {
            cardSideA = "back";
            cardSideB = "front";
        }

        switch (v.getId()) {  //начать чтение когда с else

            case R.id.btnNext:
                Log.d(LOG_TAG, "- - - - - - - next - - - - - - - - - - - - - - -");
                pos = c.getPosition();
//               Логи
//                Log.d(LOG_TAG, "- - - table - start - - -");
//                TableToLog();
//                Log.d(LOG_TAG, "- - - table - end - - -");
                c.moveToPosition(pos);
                if (c.moveToNext()) //и перейти на следующую строку
                {                   //если c.moveToNext() выдает значение, т.е. есть строка, то выполняется код ниже, иначе else
                    GetCardSideB();
//                    StringToLog ();
                } else { //если c.moveToNext() не выдает строку, т.е. последняя строка
                    GetCursorNewWords(); /*Здесь этот метод вызывается для того чтобы обновить базу данных
                    после нажания кнопки  learned записанное значение не появится пока не будет вызван курсор при помощи c = db.query(...);
                    вызов этого метода мне не очень удобен потому что данный код запускается каждый раз когда карточки доходят до последней строки в таблице
                    тогда мы переходим на первую строку а при вызове этого метода карточки каждый раз перемешиваются
                    я бы хотел просто обновить курсор чтобы получить актуальные данные без выученных слов но не перемешивать карточки
                    */
                    c.moveToFirst();//то переходим опять на первую строку
                    GetCardSideB();
//                    StringToLog ();
                }
                pos = c.getPosition();
                break;

            case R.id.btnFlip: //при помощи переменной flag эта кнопка исполняет два разных кода
                Log.d(LOG_TAG, "- - - - - - - flip - - - - - - - - - - - - - - -");

                if (flag) {   //чтобы понять что происходит начни чтение когда с "else"
                    //с этого момента код запускается при нажатии на кнопку второй раз
                    Log.d(LOG_TAG, "if . . .");
//                c.moveToPosition(pos);
//                int idColIndex = c.getColumnIndex("_id");
                    int cardSideAColIndex = c.getColumnIndex(cardSideA);
                    txtCard.setText(c.getString(cardSideAColIndex)); //устанавливаем в текст вью полученный текст
                    flag = false;

                } else {  // при первом нажатии на кнопку, действие начинается с этого момента
                    Log.d(LOG_TAG, "else . . .");
//                c.moveToPosition(pos);  //курсор устанавливается в позицию "pos", это общая переменная через которую ведется навигация
                    //первоначально в методе OnCreate установленна на 0
                    int cardSideBColIndex = c.getColumnIndex(cardSideB); //получить индекс колонки с названием из переменной cardSideB
                    txtCard.setText(c.getString(cardSideBColIndex));//устанавливаем в текст вью полученный текст
                    flag = true; //установить flag в положение true
                }
                break;

            case R.id.btnShuffle:
                c.close();
                GetCursorNewWords();
                c.moveToFirst();
                btnNext.performClick();
                break;

            case R.id.btnList:
                Intent intent = new Intent(this, ListBase.class);
                startActivity(intent);
                break;

            case R.id.btnLearned:
                Log.d(LOG_TAG, "- - - - - Learned - - - - -");

                try {
                    c.moveToPosition(pos);
                    String id = c.getString(c.getColumnIndex("_id"));
                    cv.put("learned", "1");
                    db = dbHelper.getWritableDatabase();
                    db.update("mytable", cv, "_id = ?", new String[]{id});
                    btnNext.performClick();
                }catch (Exception e){
                    txtCard.setText("нет слов для изучения");
                }

                break;

            case R.id.btnBack:
                c.moveToPosition(pos); //установить курсор в позицию "pos"
                if (c.moveToPrevious()) //и перейти на предыдущую строку
                {                   //если c.moveToPrevious() выдает значение, т.е. есть строка, то выполняется код ниже, иначе else
                    pos = c.getPosition(); //заменить значение переменной "pos" на новое, чтобы заработала кнопка "Flip"
                    int cardSideBColIndex = c.getColumnIndex(cardSideB); //получить индекс колонки с названием front
                    txtCard.setText(c.getString(cardSideBColIndex));
                    flag = true; //установить flag в положение true
                } else { //если c.moveToPrevious() не выдает строку, т.е. первая строка
                    c.moveToLast();//то переходим опять на первую строку
                    pos = c.getPosition(); //заменить значение переменной "pos" на новое, чтобы заработала кнопка "Flip"
                    int idColIndex = c.getColumnIndex("id"); //получить индекс колонки с названием id
                    int cardSideBColIndex = c.getColumnIndex(cardSideB); //получить индекс колонки с названием front
                    txtCard.setText(c.getString(cardSideBColIndex));//устанавливаем в текст вью полученный текст
                    flag = true; //установить flag в положение true
                }
                break;

            case R.id.btnAdd:
                String front = etFront.getText().toString(); // "переменной front" = "из etFront" . "присвоить значение" . "приобразованное  встроку"
                String back = etBack.getText().toString();

                // подготовим данные для вставки в виде пар: наименование столбца - значение
                cv.put  //поместить в объект cv (put стандартное значение)
                        ("front",    // в столбец с именем "front"
                                front); // значение из переменной front
                cv.put("back", back);
                cv.put("learned", "");

                GetCursorNewWords();
                c.moveToPosition(pos);
                long rowID = db.insert("mytable", null, cv); //вставляем в базу данных db, в таблицу "mytable", строку со значением из cv
                Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                etFront.setText("");
                etBack.setText("");
                UnLockReadButtons();
                btnNext.performClick();
                break;
            case R.id.btnFill:
                FillTable();
                UnLockReadButtons();
                break;

            case R.id.btnReset:
                Log.d(LOG_TAG, "- - - Reset - - -");
                db = dbHelper.getWritableDatabase();
                c = db.query("mytable", null, null, null, null, null,null);
                c.moveToFirst();

                do {
                    String id = c.getString(c.getColumnIndex("_id"));
                    cv.put("learned", "");
                    db.update("mytable", cv, "_id = ?", new String[]{id});
                }while(c.moveToNext());
                UnLockReadButtons();
                btnNext.performClick();
                break;

            case R.id.btndlt:
                Log.d(LOG_TAG, "--- delete database ---");
                db = dbHelper.getWritableDatabase();
                db.execSQL("drop table mytable");   //удаляем таблицу
//            db.execSQL("DROP TABLE IF EXISTS " + "mytable");
                // создаем таблицу с полями
                Log.d(LOG_TAG, "--- create database ---");
                db.execSQL("create table mytable ("
                        + "_id integer primary key autoincrement,"
                        + "front text,"
                        + "back text,"
                        + "learned bit"
                        + ");"
                );
                break;

        }
        db.close();
    }

    public void FillTable() { //записать в таблицу одну строчку с произвольным номером
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query("mytable", null, null, null, null, null, null);
        ContentValues cv = new ContentValues();
        c.moveToLast();
        int number;

        if (c.getPosition() != -1) {
            int idColIndex = c.getColumnIndex("_id"); //получить индекс колонки с названием id
            number = c.getInt(idColIndex) + 1;
        } else {
            number = 1;
        }

        cv.put("front", "word #" + number);
        cv.put("back", "слово №" + number);
        cv.put("learned", "");

        long rowID = db.insert("mytable", null, cv);
        Log.d(LOG_TAG, "id = " + rowID + ", слово №" + number);
        db.close();
//        GetCount();
    }

    public void DellFromTable() {

    }

    public void GetCount() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query("mytable", null, null, null, null, null, null);
        if (c.moveToFirst()) {
            if (true) {
                int rowCount = c.getCount();
                Log.d(LOG_TAG, "всего строк: " + rowCount);
                db.close();
            } else {
                Log.d(LOG_TAG, "всего строк: " + 0);
            }
        } else {
            Log.d(LOG_TAG, "нет базы данных");
        }
    }

    public void onSwitchLangClick(View view) {
        switchLangToggle();
        setCardSides();
    }

    public void onChLockClick(View view) {
        if (chLock.isChecked()) {
            chLock.setText("Locked");
            btnAdd.setEnabled(false);
            btnFill.setEnabled(false);
            btndlt.setEnabled(false);
        } else {
            chLock.setText("Unlocked");
            btnAdd.setEnabled(true);
            btnFill.setEnabled(true);
            btndlt.setEnabled(true);
        }
    }

    public void switchLangToggle() {
        if (switchLang == true) {
            switchLang = false;

            switchLangBtn.setText("Eng");
        } else {
            switchLang = true;
            switchLangBtn.setText("Рус");
        }
    }

    public void changeFlag() {
        if (flag == true) {
            flag = false;
        } else {
            flag = true;
        }
    }

    public void setCardSides() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query("mytable", null, null, null, null, null, null);
        c.moveToPosition(pos);
        pos = c.getPosition(); //заменить значение переменной "pos" на новое, чтобы заработала кнопка "Flip"

        if (switchLang) {
            cardSideA = "front";
            cardSideB = "back";
        } else {
            cardSideA = "back";
            cardSideB = "front";
        }

        int frontColIndex = c.getColumnIndex(cardSideB); //получить индекс колонки с названием front
        txtCard.setText(c.getString(frontColIndex));
        flag = true; //установить flag в положение true
    }

    public void GetCardSideB() { //установить текст на экране из базы, колонка с названием стороны В (Front или Back)
        try {
//            pos = c.getPosition(); //заменить значение переменной "pos" на новое, чтобы заработала кнопка "Flip"
//        int idColIndex = c.getColumnIndex("_id"); //получить индекс колонки с названием id
            int cardSideBColIndex = c.getColumnIndex(cardSideB); //получить индекс колонки с названием стороны В (Front или Back)
            txtCard.setText(c.getString(cardSideBColIndex));
            flag = true; //установить flag в положение true
        }catch (Exception e){
//            Toast toast = Toast.makeText(getApplicationContext(),
//                    "ошибка см. лог",
//                    Toast.LENGTH_SHORT);
//            toast.show();
//            Log.d(LOG_TAG, "ошибка в методе GetCardSideB()");
            txtCard.setText("нет слов для изучения");
            LockReadButtons();
        }
    }

    public void GetRandomCursor() {         //  В этом методе  подключаемся к базе данных сортируем значения в произвольном порядке
        db = dbHelper.getWritableDatabase();
        c = db.query("mytable", null, null, null, null, null, "random()");
    }

    public void GetCursorNewWords() {
        /*
        В этом методе  подключаемся к базе данных фильтруем значения по колонке learned,
        если не равно 1 (что значит выученно), то возвращаем слово
        */
        db = dbHelper.getWritableDatabase();
        String [] selArg = new String[]{Integer.toString(1)}; //в таблице есть колонка learned, в ней значение "1" означает "выученно"
//            String order = "RANDOM() limit 10";
//            Log.d(LOG_TAG, "order: " + order);
        c = db.query("mytable", null, "learned != ?", selArg, null, null,"random()");
//            c = db.query("mytable", null, null, null, null, null, "random()");
    }

    public void NewTry (){
        Log.d(LOG_TAG, "- - - - - New try начало - - - - -");

        ContentValues cv = new ContentValues();
        db = dbHelper.getWritableDatabase();
        c = db.query("mytable", null, null, null, null, null,null);

        c.moveToFirst();
        do {StringToLog();}while(c.moveToNext());

        Log.d(LOG_TAG, "- - - - - -"); //разделяет код на до обновления строки и после

        c.moveToFirst();
        c.moveToNext();
        c.moveToNext();
//        c.moveToPosition(2);
        String id = c.getString(c.getColumnIndex("_id"));
        cv.put("learned", "xexe");
        db.update("mytable", cv, "_id = ?", new String[]{id});

//
        //для того чтобы база данных обновилась нужно запросить курсор
        //не знаю нужно ли для этого закрывать и открывать подключение к базе данных
        c = db.query("mytable", null, null, null, null, null,null);

        c.moveToFirst();
        do {StringToLog();}while(c.moveToNext());

        Log.d(LOG_TAG, "- - - - - New try конец - - - - -");
    }

    public void StringToLog (){
        Log.d(LOG_TAG, " | " + c.getString(c.getColumnIndex("_id")) + " | " + c.getString(c.getColumnIndex("front")) + " | " + c.getString(c.getColumnIndex("back")) +
                " | " + c.getString(c.getColumnIndex("learned")) + " |");
    }
    public void TableToLog (){
        c.moveToFirst();
        do {
            StringToLog();
        }while(c.moveToNext());
    }

    public void ReCreateTable(){
        Log.d(LOG_TAG, "--- delete database ---");
        db.execSQL("drop table mytable");   //удаляем таблицу
//            db.execSQL("DROP TABLE IF EXISTS " + "mytable");
        Log.d(LOG_TAG, "--- create database ---");
        db.execSQL("create table mytable ("
                + "_id integer primary key autoincrement,"
                + "front text,"
                + "back text,"
                + "learned bit"
                + ");"
        );
        FillTable();
        FillTable();
        FillTable();
        FillTable();
    }

    public void LockReadButtons(){
//        этот метод блокирует кнопки которые читают из базы данных
        btnNext.setEnabled(false);
        btnShuffle.setEnabled(false);
        btnFlip.setEnabled(false);
        btnBack.setEnabled(false);
    }

    public void UnLockReadButtons(){
//        этот метод РАЗблокирует кнопки которые читают из базы данных
        btnNext.setEnabled(true);
        btnShuffle.setEnabled(true);
        btnFlip.setEnabled(true);
        btnBack.setEnabled(true);
    }


}


