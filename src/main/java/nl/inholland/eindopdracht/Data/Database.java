package nl.inholland.eindopdracht.Data;

import nl.inholland.eindopdracht.Models.Item;
import nl.inholland.eindopdracht.Models.Member;
import nl.inholland.eindopdracht.Models.User;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Database {
    public List<User> users;
    public List<Item> items;
    public List<Member> members;

    public Database() {
        this.users = new ArrayList<>();
        loadUsers();

        this.items = new ArrayList<>();
        loadItems();

        this.members = new ArrayList<>();
        loadMembers();
    }

    private void loadMembers() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2002, Calendar.SEPTEMBER, 6);
        members.add(new Member(1, "Sebastiaan", "van Vliet", calendar));

        calendar.set(2002, Calendar.JUNE, 8);
        members.add(new Member(2, "Luc", "Moetwil", calendar));

        calendar.set(2002, Calendar.AUGUST, 15);
        members.add(new Member(3, "Lars", "Hartendorp", calendar));
    }

    private void loadUsers() {
        // add 2 users
        this.users.add(new User("admin", "1234"));
        this.users.add(new User("sebas", "1234"));
    }

    private void loadItems() {
        // add 2 items
        this.items.add(new Item(1, true, "Harry Potter", "J.K. Rowling"));
        this.items.add(new Item(2, true, "Lord of the Rings", "J.R.R. Tolkien"));
    }

    public String lendItem(int itemCode, int memberId) {
        // check if the item exists
        for (Item item : items) {
            if (item.getItemCode() == itemCode && item.getAvailable()) {
                // check if the member exists, only then lend the item
                Member member = getMemberById(memberId);
                if (member != null) {
                    member.addLentItem(item);
                    item.setAvailable(false);
                    item.setDateOfLending(Calendar.getInstance());
                    return null;
                }
                return "noMember";
            }
        }
        return "noItem";
    }

    public Item receiveItem(int itemCode) {
        for (Item item : items) {
            if (item.getItemCode() == itemCode && !item.getAvailable()) {
                item.setAvailable(true);
                return item;
            }
        }
        return null;
    }

    public void editItem(Item item) {
        for (Item i : items) {
            if (i.getItemCode() == item.getItemCode()) {
                i.setTitle(item.getTitle());
                i.setAuthor(item.getAuthor());
            }
        }
    }

    public void deleteItem(int itemCode) {
        for (Item item : items) {
            if (item.getItemCode() == itemCode) {
                items.remove(item);
                break;
            }
        }
    }

    public void addItem(String title, String author) {
        int itemCode = items.size() + 1;
        items.add(new Item(itemCode, true, title, author));
    }

    public void addMember(String firstName, String lastName, Calendar dateOfBirth) {
        int memberId = members.size() + 1;
        members.add(new Member(memberId, firstName, lastName, dateOfBirth));
    }

    public void deleteMember(int memberId) {
        for (Member member : members) {
            if (member.getId() == memberId) {
                members.remove(member);
                break;
            }
        }
    }

    public void editMember(Member member) {
        for (Member m : members) {
            if (m.getId() == member.getId()) {
                m.setFirstName(member.getFirstName());
                m.setLastName(member.getLastName());
                m.setDateOfBirth(member.getDateOfBirth());
            }
        }
    }

    private Member getMemberById(int memberId) {
        for (Member member : members) {
            if (member.getId() == memberId) {
                return member;
            }
        }
        return null;
    }

    public void saveDateBase() {
        try {
            FileOutputStream outputStream = new FileOutputStream("items.ser", false);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(items);
            objectOutputStream.close();

            outputStream = new FileOutputStream("members.ser", false);
            objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(members);
            objectOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
