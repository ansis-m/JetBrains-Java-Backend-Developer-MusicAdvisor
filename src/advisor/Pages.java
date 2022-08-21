package advisor;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Pages {


    private static int pageSize;
    private static List<String> output;
    private static int size;
    private static int currentIndex;



    Pages(int pageSize){
        this.pageSize = pageSize;
        currentIndex = 0;
        output = new ArrayList<>();
    }

    public static void addOutput(String entry){
        output.add(entry);
        size++;
    }

    public static void clear() {
        output.clear();
        currentIndex = 0;
        size = 0;
    }

    public static void displayNext() {

        if(currentIndex == size) {
            System.out.println("No more pages.");
            return;
        }

        int end = currentIndex + pageSize >= size? size : currentIndex + pageSize;

        for(int i = currentIndex; i < end; i++)
            System.out.println(output.get(i));

        System.out.printf("\n---Page %d of %d---\n", currentIndex/pageSize + 1, (int) Math.ceil(size/pageSize));

        currentIndex = end;

    }

    public static void displayPrev() {

        if(currentIndex == 0) {
            System.out.println("No more pages.");
            return;
        }


        int begin = currentIndex - pageSize >= 0? currentIndex - pageSize : 0;

        for(int i = begin; i < currentIndex; i++)
            System.out.println(output.get(i));

        System.out.printf("\n---Page %d of %d---\n", begin/pageSize + 1, (int) Math.ceil(size/pageSize));

        currentIndex = begin;
    }
}
