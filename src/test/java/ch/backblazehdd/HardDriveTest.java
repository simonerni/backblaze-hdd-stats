package ch.backblazehdd;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class HardDriveTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetID2FromHDD() {
        String line = "2013-04-10,WD-WCAU4A648671,WDC WD10EADS,1000204886016,0,,0,,,,,,,,0,,,,,,31475,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,22,,,,,,0,,,,,,,,,,,,,,,,,,,,,,,,,,,,";

        String id = HardDrive.getID(line);

        assertEquals("ID", "WD-WCAU4A648671", id);

    }

    @Test
    public void testGetIDFromHDD() {
        String line = "2013-04-10,6XW0SVS9,ST31500541AS,1500301910016,0,,87406718,,,,,,,,0,,,,,,28428,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,29,,,,,,0,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n";

        String id = HardDrive.getID(line);

        assertEquals("ID", "6XW0SVS9", id);

    }


    @Test
    public void stringCompareEmpty() {

        HardDrive empty = new HardDrive();

        HardDrive date1 = new HardDrive("2013-04-10", "2013-04-10");


        HardDrive merged = new HardDrive(empty, date1);
        assertEquals(date1.getMax(), merged.getMax());
        assertEquals(date1.getMin(), merged.getMin());

    }

    @Test
    public void stringCompareNewMin() {


        HardDrive date1 = new HardDrive("2013-04-10", "2013-04-10");

        HardDrive date2 = new HardDrive("2011-04-10", "2013-04-10");


        HardDrive merged = new HardDrive(date1, date2);
        assertEquals(date1.getMax(), merged.getMax());
        assertEquals(date2.getMin(), merged.getMin());

    }

    @Test
    public void stringCompareNewMaxAndMin() {


        HardDrive date1 = new HardDrive("2013-04-10", "2015-04-10");

        HardDrive date2 = new HardDrive("2011-04-10", "2013-04-10");


        HardDrive merged = new HardDrive(date1, date2);
        assertEquals(date1.getMax(), merged.getMax());
        assertEquals(date2.getMin(), merged.getMin());

    }

    @Test
    public void mergedModel() {


        HardDrive empty = new HardDrive();

        HardDrive hdd = new HardDrive("2013-04-10,9VS3FM1J,ST31500341AS,1500301910016,1,,222508045,,,,,,,,4094,,,,,,26993,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,31,,,,,,0,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n");


        HardDrive merged = new HardDrive(empty, hdd);
        assertEquals("Max", hdd.getMax(), merged.getMax());
        assertEquals("Min", hdd.getMin(), merged.getMin());
        assertEquals("Model", hdd.getModel(), merged.getModel());
        assertEquals("Death", hdd.isDead(), merged.isDead());

    }

    @Test
    public void testHardDriveLineParse() {

        String line = "2013-04-10,MJ0351YNG9Z7LA,Hitachi HDS5C3030ALA630,3000592982016,0,,0,,,,,,,,0,,,,,,3593,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,26,,,,,,0,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n";

        HardDrive hardDrive = new HardDrive(line);
        assertEquals("Minimal Date", "2013-04-10", hardDrive.getMin());
        assertEquals("Maximal Date", "2013-04-10", hardDrive.getMax());

        assertEquals("Model", "Hitachi HDS5C3030ALA630", hardDrive.getModel());

        assertFalse("Dead", hardDrive.isDead());

    }

    @Test
    public void testHardDriveLineParseDeath() {

        String line = "2013-04-10,9VS3FM1J,ST31500341AS,1500301910016,1,,222508045,,,,,,,,4094,,,,,,26993,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,31,,,,,,0,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n";

        HardDrive hardDrive = new HardDrive(line);
        assertEquals("Minimal Date", "2013-04-10", hardDrive.getMin());
        assertEquals("Maximal Date", "2013-04-10", hardDrive.getMax());

        assertEquals("Model", "ST31500341AS", hardDrive.getModel());

        assertTrue("Dead", hardDrive.isDead());

    }

    @Test
    public void testCSVLine() {

        HardDrive hd = new HardDrive("2013-04-11", "2013-04-13", "MyModel", true);

        assertEquals("2,1,MyModel", hd.getCSVLine());



    }
}