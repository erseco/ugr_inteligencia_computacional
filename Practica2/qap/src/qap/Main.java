/*  
 *  Inteligencia computacional
 *  Master Profesional en Ingeniería Informática
 * 
 *  2018 © Copyleft - All Wrongs Reserved
 *
 *  Ernesto Serrano <erseco@correo.ugr.es>
 * 
 */
package qap;

import java.util.LinkedHashMap;
import qap.Genetic.AlgorithmType;

/**
 * 
 * @author Ernesto Serrano
 */
public class Main {
    public static void main(String[] args) {

        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();

//        map.put("data/chr12a.dat", 9552);
//        map.put("data/nug12.dat", 578);
//        map.put("data/nug20.dat", 2570);
//        map.put("data/chr20a.dat", 2192);
//        map.put("data/bur26a.dat", 5426670);
//        map.put("data/bur26b.dat", 3817852);
//        map.put("data/lipa40a.dat", 31538);
//        map.put("data/tai60a.dat", 7205962);
        map.put("data/tai256c.dat", 44759294);        
        
        map.entrySet().forEach((entry) -> {
            for (AlgorithmType type : AlgorithmType.values()) {

                // We do many test of every algorytm
                for (int i = 0; i < 2; i++) {

                    Genetic alg = new Genetic(type, entry.getKey());

                    long startTime = System.currentTimeMillis();
                    alg.execute(entry.getValue());
                    long endTime = System.currentTimeMillis();
                    
                    System.out.println("Time: " + (endTime - startTime) / 1000);
                    System.out.println("Name:" + entry.getKey());
                    
                }
            }
        });
      
    }
    
}
