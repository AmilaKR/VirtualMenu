package com.amila.dev.virtualmenu;

public class ListItem {
    String name;
    String price;

    public ListItem(String name, String price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        if(price.contains("null")){
            return " ";
        }
        return price;
    }
}
