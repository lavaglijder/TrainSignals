package nl.ijsglijder.traincraft.signals;

import nl.ijsglijder.traincraft.TrainSignals;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SignalManager {

    HashMap<SignalVector, SignalClass> signals = new HashMap<>();
    HashMap<String, SignalClass> signalsAsString = new HashMap<>();
    HashMap<String, List<String>> signalLinks = new HashMap<>();
    HashMap<String, List<String>> signalStationLinks = new HashMap<>();
    HashMap<String, List<String>> signalSwitcherLinks = new HashMap<>();

    public SignalManager() {

    }

    public SignalClass getSignal(SignalVector vector) {
        return signals.get(vector);
    }

    public SignalClass getSignal(World world, int x, int y, int z) {
        return getSignal(new SignalVector(world, x, y, z));
    }

    public void addSignal(SignalClass signal) {
        signalsAsString.put(signal.getSignalID(), signal);
    }

    public HashMap<SignalVector, SignalClass> getSignals() {
        return signals;
    }

    public void removeSignal(SignalClass signal) {
        signal.delete();
        signals.remove(signal.getVector());
    }

    public SignalClass getSignal(String id) {
        return signalsAsString.get(id);
    }

    public void removeSignal(SignalVector vector) {
        removeSignal(getSignal(vector));
    }

    public void removeSignal(SignalClass signal, boolean deleteFromDB) {
        signal.removeDetector();
        if(deleteFromDB) {
            FileConfiguration fc = TrainSignals.getFileManager().getFile("signals.yml").getFc();

            fc.getKeys(true).forEach(s -> {
                if(s.startsWith(signal.getSignalID()+ ".")) fc.set(s, null);
            });

            fc.set(signal.getSignalID(), null);


        }
        signal.delete();
    }

    public List<String> getLinkedSignals(SignalClass signalClass) {
        return getLinkedSignals(signalClass.getSignalID());
    }
    public List<String> getLinkedSignals(String signalID) {
        return this.signalLinks.get(signalID);
    }

    public void addLink(SignalClass signalClass, List<String> signalIDList) {
        signalLinks.replace(signalClass.getSignalID(), signalIDList);
    }
    public void addLink(String signalID, List<String> signalIDList) {
        signalLinks.replace(signalID, signalIDList);
    }

    public void addLink(SignalClass signalClass, String signalID) {
        if(!signalLinks.containsKey(signalClass.getSignalID())) signalLinks.put(signalClass.getSignalID(), new ArrayList<>());
        List<String> strings = signalLinks.get(signalClass.getSignalID());
        strings.add(signalID);
        signalLinks.replace(signalClass.getSignalID(), strings);
    }

    public void addLink(String signalID, String signalID2) {
        if(!signalLinks.containsKey(signalID)) signalLinks.put(signalID, new ArrayList<>());
        List<String> strings = signalLinks.get(signalID);
        strings.add(signalID2);
        signalLinks.replace(signalID, strings);
    }

    public void removeLink(SignalClass signalClass, String signalID) {
        if(!signalLinks.containsKey(signalClass.getSignalID())) signalLinks.put(signalClass.getSignalID(), new ArrayList<>());
        List<String> strings = signalLinks.get(signalClass.getSignalID());
        strings.remove(signalID);
        signalLinks.replace(signalClass.getSignalID(), strings);
    }

    public void removeLink(String signalID, String signalID2) {
        if(!signalLinks.containsKey(signalID)) signalLinks.put(signalID, new ArrayList<>());
        List<String> strings = signalLinks.get(signalID);
        strings.remove(signalID2);
        signalLinks.replace(signalID, strings);
    }

    public void removeSignal(SignalVector vector, boolean deleteFromDB) {
        removeSignal(getSignal(vector), deleteFromDB);
    }

    /**
        Signal links
     **/
    public void addStationLink(SignalClass signalClass, String signalID) {
        if(!signalStationLinks.containsKey(signalClass.getSignalID())) signalStationLinks.put(signalClass.getSignalID(), new ArrayList<>());
        List<String> strings = signalStationLinks.get(signalClass.getSignalID());
        strings.add(signalID);
        signalStationLinks.replace(signalClass.getSignalID(), strings);
    }

    public void addStationLink(String signalID, String signalID2) {
        if(!signalStationLinks.containsKey(signalID)) signalStationLinks.put(signalID, new ArrayList<>());
        List<String> strings = signalStationLinks.get(signalID);
        strings.add(signalID2);
        signalStationLinks.replace(signalID, strings);
    }

    public void removeStationLink(SignalClass signalClass, String signalID) {
        if(!signalStationLinks.containsKey(signalClass.getSignalID())) signalStationLinks.put(signalClass.getSignalID(), new ArrayList<>());
        List<String> strings = signalStationLinks.get(signalClass.getSignalID());
        strings.remove(signalID);
        signalStationLinks.replace(signalClass.getSignalID(), strings);
    }

    public void removeStationLink(String signalID, String signalID2) {
        if(!signalStationLinks.containsKey(signalID)) signalStationLinks.put(signalID, new ArrayList<>());
        List<String> strings = signalStationLinks.get(signalID);
        strings.remove(signalID2);
        signalStationLinks.replace(signalID, strings);
    }
    public List<String> getLinkedStationSignals(SignalClass signalClass) {
        return getLinkedStationSignals(signalClass.getSignalID());
    }
    public List<String> getLinkedStationSignals(String signalID) {
        return this.signalStationLinks.get(signalID);
    }

    /**
       Switcher links
     */
    public void addSwitcherLink(SignalClass signalClass, String signalID) {
        if(!signalSwitcherLinks.containsKey(signalClass.getSignalID())) signalSwitcherLinks.put(signalClass.getSignalID(), new ArrayList<>());
        List<String> strings = signalSwitcherLinks.get(signalClass.getSignalID());
        strings.add(signalID);
        signalSwitcherLinks.replace(signalClass.getSignalID(), strings);
    }

    public void addSwitcherLink(String signalID, String signalID2) {
        if(!signalSwitcherLinks.containsKey(signalID)) signalSwitcherLinks.put(signalID, new ArrayList<>());
        List<String> strings = signalSwitcherLinks.get(signalID);
        strings.add(signalID2);
        signalSwitcherLinks.replace(signalID, strings);
    }

    public void removeSwitcherLink(SignalClass signalClass, String signalID) {
        if(!signalSwitcherLinks.containsKey(signalClass.getSignalID())) signalSwitcherLinks.put(signalClass.getSignalID(), new ArrayList<>());
        List<String> strings = signalSwitcherLinks.get(signalClass.getSignalID());
        strings.remove(signalID);
        signalStationLinks.replace(signalClass.getSignalID(), strings);
    }

    public void removeSwitcherLink(String signalID, String signalID2) {
        if(!signalSwitcherLinks.containsKey(signalID)) signalSwitcherLinks.put(signalID, new ArrayList<>());
        List<String> strings = signalSwitcherLinks.get(signalID);
        strings.remove(signalID2);
        signalSwitcherLinks.replace(signalID, strings);
    }

    public List<String> getLinkedSwitcherSignals(SignalClass signalClass) {
        return getLinkedSwitcherSignals(signalClass.getSignalID());
    }
    public List<String> getLinkedSwitcherSignals(String signalID) {
        return this.signalSwitcherLinks.get(signalID);
    }



    public HashMap<String, SignalClass> getSignalsAsString() {
        return signalsAsString;
    }

    public HashMap<String, List<String>> getSignalLinks() {
        return signalLinks;
    }

    public HashMap<String, List<String>> getSignalStationLinks() {
        return signalStationLinks;
    }

    public HashMap<String, List<String>> getSignalSwitcherLinks() {
        return signalSwitcherLinks;
    }
}
