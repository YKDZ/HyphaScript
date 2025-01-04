public class Test {
    private String name;
    private int age;

    public Test(String name, int age) {
        this.age = age;
        this.name = name;
    }

    public Test aged() {
        return new Test(name, age * age);
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        System.out.println("int");
        this.age = age;
    }

    public void charFunc(char c) {
        System.out.println(c);
    }

    public void setAge(double age) {
        System.out.println("double");
        this.age = (int) age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static void test() {
        System.out.println("Static");
    }

    @Override
    public String toString() {
        return "Test{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
