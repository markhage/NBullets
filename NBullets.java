import tester.Tester;
import javalib.worldimages.*;
import javalib.funworld.*;
import java.awt.Color;
import java.util.Random;

public class NBullets extends World {

  // setting constants
  public static final int SCREENWIDTH = 500;
  public static final int SCREENHEIGHT = 300;
  public static final int SHIPSPEEDRIGHT = 4;
  public static final int SHIPSPEEDLEFT = -4;
  public static final int BULLETSPEED = -9;
  public static final int SHIPSPAWNRATE = 24;
  public static final WorldScene BACKGROUND = new WorldScene(SCREENWIDTH, SCREENHEIGHT)
      .placeImageXY(
          new RectangleImage(SCREENWIDTH, SCREENHEIGHT, OutlineMode.SOLID, Color.LIGHT_GRAY),
          SCREENWIDTH / 2, SCREENHEIGHT / 2);

  IList<Bullet> bullets;
  IList<Ship> ships;
  int shots;
  int destroyed;
  Random rand;
  int time;

  // standard constructor
  NBullets(IList<Bullet> bullets, IList<Ship> ships, int shots, int destroyed, Random rand,
      int time) {
    this.bullets = bullets;
    this.ships = ships;
    this.shots = shots;
    this.destroyed = destroyed;
    this.rand = rand;
    this.time = time;

  }

  // test constructor (takes in a seed for the random class)
  NBullets(int shots, Random rand) {
    this(new MtList<Bullet>(), new MtList<Ship>(), shots, 0, rand, 0);
  }

  // player constructor (only takes in a number of shots
  NBullets(int shots) {
    this(new MtList<Bullet>(), new MtList<Ship>(), shots, 0, new Random(), 0);
  }

  /*
   * ONTICK
   */

  public NBullets onTick() {
    // if it is time for ships to spawn, spawn ships -> move all game pieces ->
    // remove off-screen
    // -> check for collisions -> add time to the clock
    if (this.time == NBullets.SHIPSPAWNRATE) {
      return this.spawnShips().moveObjects().removeObjects().collision().addTime();
    }
    // if it is not time to spawn ships, don't spawn them
    else {
      return this.moveObjects().removeObjects().collision().addTime();

    }
  }

  // filter collided pieces from ship and bullet lists, create bullet explosions
  // add number of ships lost to this destroyed
  public NBullets collision() {
    IList<Ship> unHitShips = this.bullets.foldr(new CheckBulletHit(), this.ships);
    IList<Bullet> bulletExplosions = this.bullets.foldr2(new BulletExplosion(), this.ships,
        this.bullets);

    return new NBullets(bulletExplosions, unHitShips, this.shots,
        this.destroyed + (this.ships.length() - unHitShips.length()), this.rand, this.time);
  }

  // add time to this game, when time reaches SHIPSPAWNRATE ships will spawn
  public NBullets addTime() {
    return new NBullets(this.bullets, this.ships, this.shots, this.destroyed, this.rand,
        this.time + 1);
  }

  // filter off-screen objects from this ships and bullets
  public NBullets removeObjects() {
    return new NBullets(this.bullets.filter(new FilterOffscreenBullet()),
        this.ships.filter(new FilterOffscreenShip()), this.shots, this.destroyed, this.rand,
        this.time);
  }

  // move this bullets and this ships
  public NBullets moveObjects() {
    return new NBullets(this.bullets.map(new MoveBullets()), this.ships.map(new MoveShips()),
        this.shots, this.destroyed, this.rand, this.time);
  }

  // spawn new ships, add them to this ships list
  public NBullets spawnShips() {

    // setting random numbers as variables
    int numShips = this.rand.nextInt(3) + 1;
    int shipLocation1 = this.rand.nextInt(12) + 1;
    int shipLocation2 = this.rand.nextInt(12) + 1;
    int shipLocation3 = this.rand.nextInt(12) + 1;

    // ensure that second ship spawns in different locations than first ship
    while (shipLocation2 == shipLocation1) {
      shipLocation2 = this.rand.nextInt(12) + 1;
    }
    // ensure that third ships spawn in different location from first and second
    // ship
    while (shipLocation2 == shipLocation3 || shipLocation1 == shipLocation3) {
      shipLocation3 = this.rand.nextInt(12) + 1;
    }

    // creating variables for ship spawns and travel directions
    MyPosn shipLocationPosn1 = new MyPosn(0, 0);
    MyPosn shipLocationPosn2 = new MyPosn(0, 0);
    MyPosn shipLocationPosn3 = new MyPosn(0, 0);

    MyPosn shipVelocity1 = new MyPosn(0, 0);
    MyPosn shipVelocity2 = new MyPosn(0, 0);
    MyPosn shipVelocity3 = new MyPosn(0, 0);

    // determining ship directions and positions
    if (shipLocation1 <= 6) {
      shipVelocity1 = new MyPosn(NBullets.SHIPSPEEDRIGHT, 0);
      shipLocationPosn1 = new MyPosn(0, (shipLocation1 * 40) + 10);
    }
    else {
      shipVelocity1 = new MyPosn(NBullets.SHIPSPEEDLEFT, 0);
      shipLocationPosn1 = new MyPosn(NBullets.SCREENWIDTH, ((shipLocation1 - 6) * 40) + 10);
    }

    if (shipLocation2 <= 6) {
      shipVelocity2 = new MyPosn(NBullets.SHIPSPEEDRIGHT, 0);
      shipLocationPosn2 = new MyPosn(0, (shipLocation2 * 40) + 10);
    }
    else {
      shipVelocity2 = new MyPosn(NBullets.SHIPSPEEDLEFT, 0);
      shipLocationPosn2 = new MyPosn(NBullets.SCREENWIDTH, ((shipLocation2 - 6) * 40) + 10);
    }

    if (shipLocation3 <= 6) {
      shipVelocity3 = new MyPosn(NBullets.SHIPSPEEDRIGHT, 0);
      shipLocationPosn3 = new MyPosn(0, (shipLocation3 * 40) + 10);
    }
    else {
      shipVelocity3 = new MyPosn(NBullets.SHIPSPEEDLEFT, 0);
      shipLocationPosn3 = new MyPosn(NBullets.SCREENWIDTH, ((shipLocation3 - 6) * 40) + 10);
    }

    // determining the number of ships to spawn, adding them to this ships list
    if (numShips == 1) {
      return new NBullets(this.bullets,
          new ConsList<Ship>(new Ship(shipLocationPosn1, shipVelocity1), this.ships), this.shots,
          this.destroyed, this.rand, 0);
    }
    else if (numShips == 2) {
      return new NBullets(this.bullets,
          new ConsList<Ship>(new Ship(shipLocationPosn1, shipVelocity1),
              new ConsList<Ship>(new Ship(shipLocationPosn2, shipVelocity2), this.ships)),
          this.shots, this.destroyed, this.rand, 0);
    }
    else {
      return new NBullets(this.bullets,
          new ConsList<Ship>(new Ship(shipLocationPosn1, shipVelocity1),
              new ConsList<Ship>(new Ship(shipLocationPosn2, shipVelocity2),
                  new ConsList<Ship>(new Ship(shipLocationPosn3, shipVelocity3), this.ships))),
          this.shots, this.destroyed, this.rand, 0);
    }
  }

  /*
   * KEY HANDLER
   */

  // shoot bullets when space is hit and this has shots
  // remove one shot from this shots
  public NBullets onKeyEvent(String key) {
    if (key.equals(" ") && this.shots > 0) {
      return new NBullets(
          new ConsList<Bullet>(
              new Bullet(new MyPosn(NBullets.SCREENWIDTH / 2, NBullets.SCREENHEIGHT),
                  new MyPosn(0, NBullets.BULLETSPEED), 2, 1),
              this.bullets),
          this.ships, this.shots - 1, this.destroyed, this.rand, this.time);
    }
    else {
      return this;
    }
  }

  /*
   * MAKE SCENE
   */

  // place ships, bullets, and text onto an empty scene
  public WorldScene makeScene() {
    WorldImage gameText = new TextImage("bullets left: " + Integer.toString(this.shots)
        + " ships destroyed: " + Integer.toString(this.destroyed), 12, Color.BLACK);
    WorldImage gameTextPinhole = gameText.movePinholeTo(new Posn((int) gameText.getWidth() / -2,
        -1 * NBullets.SCREENHEIGHT + (int) gameText.getHeight() / 2));
    WorldScene textScene = BACKGROUND.placeImageXY(gameTextPinhole, 0, 0);
    WorldScene shipScene = this.ships.foldr(new PlaceShips(), textScene);

    return this.bullets.foldr(new PlaceBullets(), shipScene);
  }

  /*
   * worldEnds
   */

  // end the world when player out of shots and there are no bullets remaining
  // on-screen
  public WorldEnd worldEnds() {
    if (this.shots == 0 && this.bullets.empty()) {
      return new WorldEnd(true, this.makeAFinalScene());
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }

  // final scene, displays number of ships destroyed
  public WorldScene makeAFinalScene() {
    WorldImage text = new TextImage("Destroyed: " + Integer.toString(this.destroyed), 40,
        Color.DARK_GRAY);

    return new WorldScene(NBullets.SCREENWIDTH, NBullets.SCREENHEIGHT).placeImageXY(text,
        NBullets.SCREENWIDTH / 2, NBullets.SCREENHEIGHT / 2);
  }

}

// represents a position
class MyPosn extends Posn {

  // constructor
  MyPosn(int x, int y) {
    super(x, y);
  }

  // constructor to convert from a Posn to MyPosn
  MyPosn(Posn p) {
    this(p.x, p.y);
  }

  /*
   * Fields: this.x ... int this.y ... int
   * 
   * Methods: add(MyPosn) ... MyPosn isOffscreen(int, int) ... boolean returnX()
   * ... int returnY() ... int sameAs(MyPosn) ... boolean
   */

  // adds a MyPosn to this MyPosn
  public MyPosn add(MyPosn that) {
    return new MyPosn(this.x + that.x, this.y + that.y);
  }

  // is this MyPosn off-screen?
  public boolean isOffscreen(int width, int height) {
    return (this.x > width) || (this.x < 0) || (this.y > height) || (this.y < 0);
  }

  // return this x
  public int returnX() {
    return this.x;
  }

  // return this y
  public int returnY() {
    return this.y;
  }

  // is this myposn the same as a given myposn
  public boolean sameAs(MyPosn that) {
    return this.x == that.x && this.y == that.y;
  }
}

/*
 * OBJECTS
 */

// represents a ship
class Ship {
  MyPosn position;
  MyPosn velocity;

  // default constructor
  Ship(MyPosn position, MyPosn velocity) {
    this.position = position;
    this.velocity = velocity;
  }

  /*
   * Fields: this.postion ... MyPosn this.velocity ... MyPosn
   * 
   * Methods: draw() ... WorldImage place(WorldScene) ... WorldScene
   * wasShot(Bullet) ... boolean
   */

  // draws the ship as a circle
  public WorldImage draw() {
    return new CircleImage(10, OutlineMode.SOLID, Color.CYAN);
  }

  // draw this onto a world scene
  public WorldScene place(WorldScene ws) {
    return ws.placeImageXY(this.draw(), this.position.returnX(), this.position.returnY());
  }

  // is this ship touching a given bullet
  public boolean wasShot(Bullet b) {
    return Math.hypot(this.position.returnX() - b.position.returnX(),
        this.position.returnY() - b.position.returnY()) < (10 + b.size);
  }
}

// represents a bullet
class Bullet {
  MyPosn position;
  MyPosn velocity;
  int size;
  int level;

  // default constructor, limits size to 5, limits level to 12 to prevent map
  // overflow
  Bullet(MyPosn position, MyPosn velocity, int size, int level) {
    this.position = position;
    this.velocity = velocity;
    if (size <= 10) {
      this.size = size;
    }
    else {
      this.size = 10;
    }
    this.level = level;
  }

  // explosion constructor, determines size of this bullet based on level
  Bullet(MyPosn position, MyPosn velocity, int level) {
    this(position, velocity, level * 2, level);
  }

  /*
   * Fields: this.postion ... MyPosn this.velocity ... MyPosn this.size ... int
   * this.level ... int
   * 
   * Methods: draw() ... WorldImage place(WorldScene) ... WorldScene explosion()
   * ... IList<Bullet> explosionAcc(IList<Bullet>) ... IList<Bullet>
   * sameAs(Bullet) ... boolean
   */

  // draws this bullet as a circle
  public WorldImage draw() {
    return new CircleImage(this.size, OutlineMode.SOLID, Color.RED);
  }

  // places this bullet onto a world scene
  public WorldScene place(WorldScene ws) {
    return ws.placeImageXY(this.draw(), this.position.returnX(), this.position.returnY());
  }

  // creates a new list of bullets from an explosion
  public IList<Bullet> explosion() {
    return this.explosionAcc(new MtList<Bullet>());
  }

  // ACCUMULATOR: adds bullets to a list of exploded bullets
  public IList<Bullet> explosionAcc(IList<Bullet> acc) {
    double level = (double) this.level + 1.0;
    for (int i = this.level + 1; i >= 0; i--) {
      int x = (int) (NBullets.BULLETSPEED * Math.cos(Math.toRadians((double) i * (360.0 / level))));
      int y = (int) (NBullets.BULLETSPEED * Math.sin(Math.toRadians((double) i * (360.0 / level))));
      MyPosn vel = new MyPosn(x, y);
      Bullet bullet = new Bullet(this.position, vel, this.level + 1);
      acc = new ConsList<Bullet>(bullet, acc);
    }
    return acc;
  }

  // returns true if this bullet has the same field values as a given bullet
  public boolean sameAs(Bullet that) {
    return this.position.sameAs(that.position) && this.velocity.sameAs(that.velocity)
        && this.size == that.size && this.level == that.level;
  }
}

/*
 * IPRED
 */

// represents a one variable predicate
interface IPred<T> {
  boolean apply(T t);
}

// filter off-screen ships
class FilterOffscreenShip implements IPred<Ship> {
  public boolean apply(Ship ship) {
    return !(ship.position.isOffscreen(NBullets.SCREENWIDTH, NBullets.SCREENHEIGHT));
  }
}

// filter off-screen bullets
class FilterOffscreenBullet implements IPred<Bullet> {
  public boolean apply(Bullet bullet) {
    return !(bullet.position.isOffscreen(NBullets.SCREENWIDTH, NBullets.SCREENHEIGHT));
  }
}

/*
 * IPRED 2
 */

// represents a two variable predicate
interface IPred2<T, U> {
  boolean apply(T t, U u);
}

// filter out ship if the ship was hit by the bullet
class FilterShotShip implements IPred2<Ship, Bullet> {
  public boolean apply(Ship ship, Bullet bullet) {
    return !(ship.wasShot(bullet));
  }
}

// filter out bullet if it is the same as a given bullet
class RemoveBullet implements IPred2<Bullet, Bullet> {
  public boolean apply(Bullet bulletthis, Bullet bulletthat) {
    return !(bulletthis.sameAs(bulletthat));
  }

}

/*
 * IFUNC
 */

// represents a one argument function
interface IFunc<A, R> {
  R apply(A arg);
}

// move ships
class MoveShips implements IFunc<Ship, Ship> {
  public Ship apply(Ship ship) {
    return new Ship(ship.position.add(ship.velocity), ship.velocity);
  }
}

// move bullets
class MoveBullets implements IFunc<Bullet, Bullet> {
  public Bullet apply(Bullet bullet) {
    return new Bullet(bullet.position.add(bullet.velocity), bullet.velocity, bullet.size,
        bullet.level);
  }
}

/*
 * IFUNC2
 */

// represents a two argument function
interface IFunc2<A1, A2, R> {
  R apply(A1 arg1, A2 arg2);
}

// places ships on a world scene
class PlaceShips implements IFunc2<Ship, WorldScene, WorldScene> {
  public WorldScene apply(Ship ship, WorldScene ws) {
    return ship.place(ws);
  }
}

//places bullets on a world scene
class PlaceBullets implements IFunc2<Bullet, WorldScene, WorldScene> {
  public WorldScene apply(Bullet bullet, WorldScene ws) {
    return bullet.place(ws);
  }
}

// filters out ships hit by bullets
class CheckBulletHit implements IFunc2<Bullet, IList<Ship>, IList<Ship>> {
  public IList<Ship> apply(Bullet bullet, IList<Ship> ships) {
    return ships.filter2terminate(new FilterShotShip(), bullet);
  }
}

/*
 * IFUNC3
 */

// represents a three argument function
interface IFunc3<A1, A2, A3, R> {
  R apply(A1 arg1, A2 arg2, A3 arg3);
}

// explodes a bullet if it is contact with any ships
// removes the bullet from the game if it explodes
class BulletExplosion implements IFunc3<Bullet, IList<Ship>, IList<Bullet>, IList<Bullet>> {
  public IList<Bullet> apply(Bullet bullet, IList<Ship> ships, IList<Bullet> base) {

    // creating a list with only the current bullet to use in foldr
    IList<Bullet> thisbullet = new ConsList<Bullet>(bullet, new MtList<Bullet>());
    // creating list of remaining ships to compare to
    IList<Ship> remainingships = thisbullet.foldr(new CheckBulletHit(), ships);

    // if no ships were hit by this bullet...
    if (ships.length() == remainingships.length()) {
      return base;
    }
    else {
      IList<Bullet> newBase = base.append(bullet.explosion());
      return newBase.filter2(new RemoveBullet(), bullet);

    }
  }
}

/*
 * ILIST
 */

// represents a list of some T
interface IList<T> {
  // filters this list based on a predicate
  IList<T> filter(IPred<T> pred);

  // applies a function to every element in this list
  <U> IList<U> map(IFunc<T, U> func);

  // applies a function to every element in this list, starting with a base-case
  // and adding to the base-case
  <U> U foldr(IFunc2<T, U, U> func, U base);

  // applies a two argument function to every element in this list
  // starts with a base-case and adds on to the base
  <U, V> V foldr2(IFunc3<T, U, V, V> func, U other, V base);

  // is this list empty?
  boolean empty();

  // filters a list based on a two variable predicate
  <U> IList<T> filter2(IPred2<T, U> pred, U other);

  // filters a list based on a two variable predicate, ends when one item has been
  // filtered
  <U> IList<T> filter2terminate(IPred2<T, U> pred, U other);

  // returns the length of this list
  int length();

  // appends that list to the end of this list
  IList<T> append(IList<T> that);

}

// represents an empty list of T
class MtList<T> implements IList<T> {

  // filters a list, this list is empty so returns an empty list
  public IList<T> filter(IPred<T> pred) {
    return this;
  }

  // applies a function to a list, this list is empty so returns an empty list
  public <U> IList<U> map(IFunc<T, U> func) {
    return new MtList<U>();
  }

  // applies a function and adds to the base, this is the end of the list so
  // returns the base
  public <U> U foldr(IFunc2<T, U, U> func, U base) {
    return base;
  }

  // this list is empty, returns true
  public boolean empty() {
    return true;
  }

  // filters a list, this list is empty so returns an empty list
  public <U> IList<T> filter2(IPred2<T, U> pred, U other) {
    return this;
  }

  // filters a list, this list is empty so returns an empty list
  public <U> IList<T> filter2terminate(IPred2<T, U> pred, U other) {
    return this;
  }

  // the length of this list is 0 because it is empty
  public int length() {
    return 0;
  }

  // this is the end of the list, so now add the given list
  public IList<T> append(IList<T> that) {
    return that;
  }

  // applies a function and adds to the base, this is the end of the list so
  // returns the base
  public <U, V> V foldr2(IFunc3<T, U, V, V> func, U other, V base) {
    return base;
  }

}

// represents  a non-empty list of T
class ConsList<T> implements IList<T> {
  T first;
  IList<T> rest;

  ConsList(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }

  /*
   * Fields: this.first ... T this.rest ... IList<T>
   * 
   * Methods: filter(IPred<T>) ... IList<T> map(IFunc<T, U>) ... IList<T>
   * foldr(IFunc2<T, U, U>, U) ... U filter2(IPred2<T, U>, U) ... IList<T>
   * filter2terminate(IPred2<T, U>, U) ... IList<T> foldr2(IFunc3<T, U, V, V>, U,
   * V) ... V
   * 
   * empty() ... boolean length() ... int append(IList<T>) ... IList<T>
   */

  // filters based on a predicate
  public IList<T> filter(IPred<T> pred) {
    if (pred.apply(this.first)) {
      return new ConsList<T>(this.first, this.rest.filter(pred));
    }
    else {
      return this.rest.filter(pred);
    }
  }

  // applies a function to every element in this list
  public <U> IList<U> map(IFunc<T, U> func) {
    return new ConsList<U>(func.apply(this.first), this.rest.map(func));
  }

  // applies a two argument function to every element in this, adds results onto a
  // base-case
  public <U> U foldr(IFunc2<T, U, U> func, U base) {
    return func.apply(this.first, this.rest.foldr(func, base));
  }

  public boolean empty() {
    return false;
  }

  // filters based on a two argument predicate - terminates after
  public <U> IList<T> filter2(IPred2<T, U> pred, U other) {
    if (pred.apply(this.first, other)) {
      return new ConsList<T>(this.first, this.rest.filter2(pred, other));
    }
    else {
      return this.rest.filter2(pred, other);
    }
  }

  // filters based on a two argument predicate - terminates after
  public <U> IList<T> filter2terminate(IPred2<T, U> pred, U other) {
    if (pred.apply(this.first, other)) {
      return new ConsList<T>(this.first, this.rest.filter2(pred, other));
    }
    else {
      return this.rest;
    }
  }

  // returns the length of this list
  public int length() {
    return 1 + this.rest.length();
  }

  // adds a given list to the end of this list
  public IList<T> append(IList<T> that) {
    return new ConsList<T>(this.first, this.rest.append(that));
  }

  // applies a three argument function to every element in this, adds results onto
  // a base-case
  public <U, V> V foldr2(IFunc3<T, U, V, V> func, U other, V base) {
    return func.apply(this.first, other, this.rest.foldr2(func, other, base));
  }

}

class ExamplesNBullet {

  // ship examples
  Ship shipa = new Ship(new MyPosn(50, 50), new MyPosn(2, 2));
  Ship shipb = new Ship(new MyPosn(75, 75), new MyPosn(1, 1));

  // bullet examples
  Bullet bulleta = new Bullet(new MyPosn(50, 50), new MyPosn(2, 2), 10, 1);
  Bullet bulletb = new Bullet(new MyPosn(75, 75), new MyPosn(1, 1), 10, 1);

  // ship list examples
  IList<Ship> listship1 = new ConsList<Ship>(shipa, new ConsList<Ship>(shipb, new MtList<Ship>()));
  IList<Ship> mtshiplist = new MtList<Ship>();

  // bullet list examples
  IList<Bullet> listbullet1 = new ConsList<Bullet>(bulleta,
      new ConsList<Bullet>(bulletb, new MtList<Bullet>()));
  IList<Bullet> mtbulletlist = new MtList<Bullet>();

  // NBullet examples
  NBullets nbullet1 = new NBullets(this.listbullet1, this.listship1, 10, 10, new Random(1), 20);

  // construct NBullets with seed 1
  boolean testBigBang(Tester t) {
    NBullets w = new NBullets(10, new Random(1));
    int worldWidth = NBullets.SCREENWIDTH;
    int worldHeight = NBullets.SCREENHEIGHT;
    double tickRate = 1.0 / 28.0;
    return w.bigBang(worldWidth, worldHeight, tickRate);
  }

  boolean testLength(Tester t) {

    return t.checkExpect(this.listship1.length(), 2) && t.checkExpect(this.listbullet1.length(), 2);
  }

  boolean testAppend(Tester t) {

    return t.checkExpect(this.listship1.append(this.listship1),
        new ConsList<Ship>(shipa, new ConsList<Ship>(shipb, new MtList<Ship>()))
            .append(new ConsList<Ship>(shipa, new ConsList<Ship>(shipb, new MtList<Ship>()))))
        && t.checkExpect(this.mtshiplist.append(listship1),
            new ConsList<Ship>(shipa, new ConsList<Ship>(shipb, new MtList<Ship>())))
        && t.checkExpect(this.listbullet1.append(mtbulletlist),
            new ConsList<Bullet>(bulleta, new ConsList<Bullet>(bulletb, new MtList<Bullet>())));

  }

  boolean testApply(Tester t) {

    return t.checkExpect(new PlaceShips().apply(this.shipa, new WorldScene(500, 300)),
        this.shipa.place(new WorldScene(500, 300)))
        && t.checkExpect(new PlaceBullets().apply(this.bulleta, new WorldScene(200, 200)),
            this.bulleta.place(new WorldScene(200, 200)))
        && t.checkExpect(new CheckBulletHit().apply(this.bulletb, this.listship1),
            this.listship1.filter2terminate(new FilterShotShip(), this.bulletb))
        && t.checkExpect(
            new BulletExplosion().apply(this.bulleta, this.listship1, this.listbullet1),
            this.listbullet1.append(this.bulleta.explosion()).filter2(new RemoveBullet(),
                this.bulleta))
        && t.checkExpect(new MoveShips().apply(this.shipa),
            new Ship(this.shipa.position.add(this.shipa.velocity), this.shipa.velocity))
        && t.checkExpect(new MoveBullets().apply(this.bulleta),
            new Bullet(this.bulleta.position.add(this.bulleta.velocity), this.bulleta.velocity,
                this.bulleta.size, this.bulleta.level))
        && t.checkExpect(new RemoveBullet().apply(this.bulleta, this.bulletb),
            !(this.bulleta.sameAs(this.bulletb)))
        && t.checkExpect(new FilterShotShip().apply(this.shipb, this.bulletb),
            !this.shipb.wasShot(this.bulletb))
        && t.checkExpect(new FilterOffscreenBullet().apply(this.bulleta),
            !(this.bulleta.position.isOffscreen(NBullets.SCREENWIDTH, NBullets.SCREENHEIGHT)))
        && t.checkExpect(new FilterOffscreenShip().apply(this.shipb),
            !(this.shipb.position.isOffscreen(NBullets.SCREENWIDTH, NBullets.SCREENHEIGHT)))
        && t.checkExpect(new BulletExplosion().apply(bulleta, listship1, listbullet1),
            listbullet1.append(bulleta.explosion()).filter2(new RemoveBullet(), bulleta));

  }

  boolean testAddTime(Tester t) {

    return t.checkExpect(this.nbullet1.addTime(),
        new NBullets(this.listbullet1, this.listship1, 10, 10, new Random(1), 21));

  }

  boolean testRemoveObjects(Tester t) {

    return t.checkExpect(this.nbullet1.removeObjects(),
        new NBullets(this.listbullet1.filter(new FilterOffscreenBullet()),
            this.listship1.filter(new FilterOffscreenShip()), 10, 10, new Random(1), 20));

  }

  boolean testMoveObjects(Tester t) {

    return t.checkExpect(this.nbullet1.moveObjects(),
        new NBullets(this.listbullet1.map(new MoveBullets()), this.listship1.map(new MoveShips()),
            10, 10, new Random(1), 20));
  }

  boolean testSameAs(Tester t) {

    return t.checkExpect(this.bulleta.sameAs(this.bulletb), false)
        && t.checkExpect(this.bulleta.sameAs(this.bulleta), true);

  }

  boolean testDraw(Tester t) {

    return t.checkExpect(this.shipa.draw(), new CircleImage(10, OutlineMode.SOLID, Color.CYAN))
        && t.checkExpect(this.bulleta.draw(), new CircleImage(10, OutlineMode.SOLID, Color.RED));

  }

  boolean testPlace(Tester t) {

    return t.checkExpect(this.shipa.place(new WorldScene(200, 200)),
        new WorldScene(200, 200).placeImageXY(this.shipa.draw(), this.shipa.position.returnX(),
            this.shipa.position.returnY()));

  }

  boolean testWasShot(Tester t) {

    return t.checkExpect(this.shipa.wasShot(this.bulletb), false);
  }

  boolean testIsOffScreen(Tester t) {

    return t.checkExpect(new MyPosn(500, 500).isOffscreen(200, 200), true)
        && t.checkExpect(new MyPosn(200, 250).isOffscreen(500, 500), false);

  }

  boolean testOnKeyEvent(Tester t) {

    return t.checkExpect(this.nbullet1.onKeyEvent(" "), new NBullets(
        new ConsList<Bullet>(new Bullet(new MyPosn(NBullets.SCREENWIDTH / 2, NBullets.SCREENHEIGHT),
            new MyPosn(0, NBullets.BULLETSPEED), 2, 1), this.listbullet1),
        this.listship1, 10 - 1, 10, new Random(1), 20))
        && t.checkExpect(this.nbullet1.onKeyEvent("b"), this.nbullet1);

  }

  boolean testOnTick(Tester t) {

    return t.checkExpect(this.nbullet1.onTick(),
        this.nbullet1.moveObjects().removeObjects().collision().addTime());

  }
}
