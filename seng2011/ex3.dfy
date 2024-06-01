datatype Bases = A | C | G | T

method Exchanger(s: seq<Bases>, x:nat, y:nat) returns (t: seq<Bases>)
requires 0<=x< |s| && 0<=y< |s|
requires 0 < |s|
ensures 0<=x< |t| && 0<=y< |t|
ensures |t| == |s|
ensures forall i | 0<=i< |t| :: (i != x && i != y) ==> t[i] == s[i]
ensures t[x] == s[y] && t[y] == s[x]
ensures multiset(t[..]) == multiset(s[..])
ensures multiset(s[..]) == multiset(old(s[..]))
{
    t := s;
    var temp:Bases := t[x];
    t := t[x := t[y]];
    t := t[y := temp];
}


method TestExchanger()
{
    var a:seq<Bases> := [A, C, A, T];
    var b:seq<Bases> := Exchanger(a, 2, 3);
    assert b[..] == [A, C, T, A];

    var c:seq<Bases> := Exchanger(a, 3, 2);
    assert b == c;
    c := Exchanger(b, 1, 1);
    assert b == c;
    c := Exchanger(b, 2, 3);
    assert a == c;

    var d:seq<Bases> := Exchanger(a, 0, 1);
    assert d == [C, A, A, T];
    
    var e:seq<Bases> := [A, A, C, C, G, G, T, T];
    var f:seq<Bases> := Exchanger(e, 0, 7);
    assert f == [T, A, C, C, G, G, T, A];

    var g:seq<Bases> := Exchanger(f, 1, 6);
    assert g == [T, T, C, C, G, G, A, A];

    var h:seq<Bases> := Exchanger(g, 2, 5);
    assert h == [T, T, G, C, G, C, A, A];

    var i:seq<Bases> := [A];
    var j:seq<Bases> := Exchanger(i, 0, 0);
    assert i == j;
}


predicate bordered(s:seq<Bases>)
{   
    forall i, j, k, l | 0<=i< j< k< l< |s| ::
    (
        (s[i] != s[j] && s[i]==A) ==>
        (s[j] != s[k] && s[j]==C) ==>
        (s[k] != s[l] && s[k]==G) ==>
        s[l]==T
    )
}


method TestBordered()
{
    var a:seq<Bases> := [A, C, G, T];
    assert a == [A, C, G, T];
    assert bordered(a);

    var b:seq<Bases> := [A];
    assert bordered(b);

    var c:seq<Bases> := [A, A, C, T, T];
    assert c == [A, A, C, T, T];
    assert bordered(c);
}


method Sorter(bases: seq<Bases>) returns (sobases:seq<Bases>)
requires 0 < |bases|
ensures bordered(sobases)
ensures multiset(sobases[..]) == multiset(bases[..])
{
    sobases := bases;
    var c:int := 0;
    var next:int := 0;
    var g:int := |sobases|;
    var t:int := |sobases|;

    while next != g
    invariant 0<=c<=next<=g<=t<=|sobases|       // housekeeping
    invariant forall i::0<=i< c ==> sobases[i]==A;
    invariant forall i::c<=i< next ==> sobases[i]==C;
    invariant forall i::g<=i< t ==> sobases[i]==G;
    invariant forall i::t<=i< |sobases| ==> sobases[i]==T;
    invariant multiset(sobases[..]) == multiset(bases[..])
    decreases t, g, g - next
    {
        match (sobases[next])
        {
            case A => 
                sobases := Exchanger(sobases, next, c);
                c := c + 1;
                next := next + 1;
            case C => next := next + 1;
            case G => 
                g := g - 1;
                sobases := Exchanger(sobases, next, g);
            case T => 
                if g == t { g := g - 1; }
                t := t - 1;
                sobases := Exchanger(sobases, next, t);
                        
        }
    }
}


method TestSorter()
{
    var a:seq<Bases> := [G, A, T];
    assert a == [G, A, T];
    var b:seq<Bases> := Sorter(a);
    assert bordered(b);
    assert multiset(b) == multiset(a);

    var c:seq<Bases> := [A, C, T];
    assert c == [A, C, T];
    var d:seq<Bases> := Sorter(c);
    assert bordered(d);
    assert multiset(d) == multiset(c);

    var e:seq<Bases> := [C, C, C];
    assert e == [C, C, C];
    var f:seq<Bases> := Sorter(e);
    assert bordered(f);
    assert multiset(f) == multiset(e);

    var g:seq<Bases> := [T, C, G, C, A, T, G, A];
    assert g == [T, C, G, C, A, T, G, A];
    var h:seq<Bases> := Sorter(g);
    assert bordered(h);
    assert multiset(h) == multiset(g);

    var i:seq<Bases> := [A];
    assert i == [A];
    var j:seq<Bases> := Sorter(i);
    assert bordered(j);
    assert multiset(j) == multiset(i);
}


