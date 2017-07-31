package parallator;

public class Paragraph {
    public String en;
    public String ru;

    public Paragraph(String en) {
        this.en = en;
    }

    public Paragraph(String en, String ru) {
        this.en = en;
        this.ru = ru;
    }

    public void setRu(String ru) {
        this.ru = ru;
    }

    public String getEn() {
        return en;
    }

    public String getRu() {
        return ru;
    }
}
