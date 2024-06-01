datatype Colour = RED | WHITE | BLUE

method FlagSort(flag:array<Colour>) returns (white:int, blue:int)
ensures 0<=white<=blue<=flag.Length
ensures forall i:: 0<=i<white ==> flag[i] == RED
ensures forall i:: white<=i<blue ==> flag[i] == WHITE
ensures forall i:: blue<=i<flag.Length ==> flag[i] == BLUE
ensures multiset(flag[..]) == multiset(old(flag[..])) 
modifies flag
{
	var next := 0;
	white := 0;
	blue := flag.Length;
	while next != blue
	invariant 0<=white<=next<=blue<=flag.Length
	invariant forall i:: 0<=i<white ==> flag[i] == RED
	invariant forall i:: white<=i<next ==> flag[i] == WHITE
	invariant forall i:: blue<=i<flag.Length ==> flag[i] == BLUE
	invariant multiset(flag[..]) == multiset(old(flag[..]))
    decreases blue - next
	{
		match (flag[next])
		{
			case RED => flag[next], flag[white] := flag[white], flag[next];
						next := next + 1;
						white := white + 1; 
			case WHITE => next := next + 1;
			case BLUE => blue := blue - 1;
						flag[next], flag[blue] := flag[blue], flag[next];
		}
	}
} 
