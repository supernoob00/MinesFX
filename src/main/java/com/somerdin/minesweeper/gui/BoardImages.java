package com.somerdin.minesweeper.gui;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.SVGLoader;

import java.net.URL;

public final class BoardImages {
    public static URL URL_DEFAULT_1;
    public static URL URL_DEFAULT_2;

    public static final SVGDocument DEFAULT_1;
    public static final SVGDocument DEFAULT_2;
    public static final SVGDocument DEFAULT_3;
    public static final SVGDocument DEFAULT_4;
    public static final SVGDocument DEFAULT_5;
    public static final SVGDocument DEFAULT_6;
    public static final SVGDocument DEFAULT_7;
    public static final SVGDocument DEFAULT_8;
    public static final SVGDocument DEFAULT_MINE;
    public static final SVGDocument DEFAULT_EXPLODED;
    public static final SVGDocument DEFAULT_FLAG;
    public static final SVGDocument DEFAULT_MAYBE;

    static {
        String BASE_URL_DEFAULT = "/classic/";
        URL_DEFAULT_1 = BoardImages.class.getResource(BASE_URL_DEFAULT + "1mines.svg");
        URL_DEFAULT_2 = BoardImages.class.getResource(BASE_URL_DEFAULT + "2mines.svg");
        URL URL_DEFAULT_3 = BoardImages.class.getResource(BASE_URL_DEFAULT + "3mines.svg");
        URL URL_DEFAULT_4 = BoardImages.class.getResource(BASE_URL_DEFAULT + "4mines.svg");
        URL URL_DEFAULT_5 = BoardImages.class.getResource(BASE_URL_DEFAULT + "5mines.svg");
        URL URL_DEFAULT_6 = BoardImages.class.getResource(BASE_URL_DEFAULT + "6mines.svg");
        URL URL_DEFAULT_7 = BoardImages.class.getResource(BASE_URL_DEFAULT + "7mines.svg");
        URL URL_DEFAULT_8 = BoardImages.class.getResource(BASE_URL_DEFAULT + "8mines.svg");
        URL URL_DEFAULT_MINE_EXPLODED = BoardImages.class.getResource(BASE_URL_DEFAULT + "exploded.svg");
        URL URL_DEFAULT_FLAG = BoardImages.class.getResource(BASE_URL_DEFAULT + "flag.svg");
        URL URL_DEFAULT_MAYBE = BoardImages.class.getResource(BASE_URL_DEFAULT + "maybe.svg");
        URL URL_DEFAULT_MINE = BoardImages.class.getResource(BASE_URL_DEFAULT + "mine.svg");

        SVGLoader loader = new SVGLoader();
        DEFAULT_1 = loader.load(URL_DEFAULT_1);
        DEFAULT_2 = loader.load(URL_DEFAULT_2);
        DEFAULT_3 = loader.load(URL_DEFAULT_3);
        DEFAULT_4 = loader.load(URL_DEFAULT_4);
        DEFAULT_5 = loader.load(URL_DEFAULT_5);
        DEFAULT_6 = loader.load(URL_DEFAULT_6);
        DEFAULT_7 = loader.load(URL_DEFAULT_7);
        DEFAULT_8 = loader.load(URL_DEFAULT_8);
        DEFAULT_MINE = loader.load(URL_DEFAULT_MINE);
        DEFAULT_EXPLODED = loader.load(URL_DEFAULT_MINE_EXPLODED);
        DEFAULT_FLAG = loader.load(URL_DEFAULT_FLAG);
        DEFAULT_MAYBE = loader.load(URL_DEFAULT_MAYBE);
    }
}
