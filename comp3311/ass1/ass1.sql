-- COMP3311 23T1 Assignment 1

-- Q1: amount of alcohol in the best beers

-- put any Q1 helper views/functions here

create or replace view Q1(beer, "sold in", alcohol) 
as
select 
	name, 
	cast(volume as text) || 'ml' || ' ' || cast(sold_in as text), 
	cast(cast(volume * abv / 100 as numeric(4, 1)) as text) || 'ml'
from beers
where rating > 9
;


-- Q2: beers that don't fit the ABV style guidelines

-- put any Q2 helper views/functions here

create or replace view Q2(beer, style, abv, reason)
as
select
	beers.name,
	styles.name,
	beers.abv,
	'too weak by ' || cast(cast(styles.min_abv - beers.abv as numeric(4,1)) as text) || '%'
from beers
join styles on beers.style = styles.id
where beers.abv < styles.min_abv

union

select
	beers.name, 
	styles.name, 
	beers.abv,
	'too strong by ' || cast(cast(beers.abv - styles.max_abv as numeric(4,1)) as text) || '%'
from beers
join styles on beers.style = styles.id
where beers.abv > styles.max_abv
;


-- Q3: Number of beers brewed in each country

-- put any Q3 helper views/functions here

create or replace view Q3(country, "#beers")
as
with temp_info as (
	select
		countries.name as country,
		locations.region,
		breweries.name,
		brewed_by.beer
	from brewed_by, breweries, locations, countries
	where brewed_by.brewery = breweries.id
	and breweries.located_in = locations.id
	and locations.within = countries.id
), temp_count as (
	select
		country,
		count(1) as num
	from temp_info
	group by 1
	order by 1
)
select
	countries.name,
	case
		when countries.name in (select country from temp_count) then (
			select num from temp_count where country = countries.name
		)
		else 0
	end
from countries, temp_count
group by 1,2
;


-- Q4: Countries where the worst beers are brewed

-- put any Q4 helper views/functions here

create or replace view Q4(beer, brewery, country)
as
with temp_info as (
	select
		countries.name as country,
		locations.region,
		breweries.name as brewery,
		brewed_by.beer,
		beers.name,
		beers.rating
	from beers, brewed_by, breweries, locations, countries
	where beers.id = brewed_by.beer
	and brewed_by.brewery = breweries.id
	and breweries.located_in = locations.id
	and locations.within = countries.id
)
select 
	name,
	brewery,
	country
from temp_info
where rating < 3
;

-- Q5: Beers that use ingredients from the Czech Republic

-- put any Q5 helper views/functions here

create or replace view Q5(beer, ingredient, "type")
as
with temp_info as (
	select
		countries.name as country,
		ingredients.origin,
		ingredients.name as ing_name,
		ingredients.itype,
		contains.ingredient,
		beers.name
	from beers, contains, ingredients, countries
	where beers.id = contains.beer
	and contains.ingredient = ingredients.id
	and ingredients.origin = countries.id
)
select 
	name, 
	ing_name,
	itype
from temp_info
where country = 'Czech Republic'
;

-- Q6: Beers containing the most used hop and the most used grain

-- put any Q6 helper views/functions here

create or replace view Q6(beer)
as
with temp_info as (
	-- all ingredientss in all beers
	select
		ingredients.name as ing_name,
		ingredients.itype,
		contains.ingredient,
		beers.name
	from beers, contains, ingredients
	where (ingredients.itype = 'hop' or ingredients.itype = 'grain')
	and beers.id = contains.beer
	and contains.ingredient = ingredients.id
), 
temp_count as (
	-- count of beers for each ingredient
	select
		ing_name,
		itype,
		count(1) as num
	from temp_info
	group by 1,2
	order by 3 desc
), 
max_count as (
	-- find most popular hop and most popular grain
	select
		ing_name, 
		itype,
		num
	from temp_count
	where num = (select max(num) from temp_count where itype = 'hop')
	and itype = 'hop'

	union 

	select
		ing_name, 
		itype,
		num
	from temp_count
	where num = (select max(num) from temp_count where itype = 'grain')
	and itype = 'grain'
), 
beers_max_count as (
	-- beers containing most popular hop or most popular grain
	select
		name, 
		ing_name
	from temp_info
	where ing_name in (select ing_name from max_count)
)
-- beers containing most popular hop AND most popular grain
select distinct
	name
from temp_info
where (select count(*) from beers_max_count where name = temp_info.name) = 2
;


-- Q7: Breweries that make no beer

-- put any Q7 helper views/functions here

create or replace view Q7(brewery)
as

select name
from breweries

except

select breweries.name
from beers, brewed_by, breweries
where beers.id = brewed_by.beer
and brewed_by.brewery = breweries.id
;

-- Q8: Function to give "full name" of beer

-- put any Q8 helper views/functions here

create or replace function
	Q8(beer_id integer) returns text
as
$$
declare 
	i integer;
	r record;
	result text := '';
begin

	-- Get all breweries for the beer
	for r in (
		select
			regexp_replace(breweries.name, ' (Beer|Brew).*$', '') as brew_name, 
			beers.name as beer
		from beers, brewed_by, breweries
		where beers.id = beer_id
		and beers.id = brewed_by.beer
		and brewed_by.brewery = breweries.id
	) loop
		if (result != '') then
			result := result || '+ ';	-- handle multiple breweries
		end if;
		result := result || r.brew_name || ' ';
	end loop;

	if (result = '') then
		return 'No such beer';
	end if;

	-- Get beers
	result = result || r.beer;

	return result;
end;
$$ language plpgsql
;

-- Q9: Beer data based on partial match of beer name

drop type if exists BeerData cascade;
create type BeerData as (beer text, brewer text, info text);


-- put any Q9 helper views/functions here

create or replace function
	Q9(partial_name text) returns setof BeerData
as
$$
declare
	r record;	-- iterator record for beers
	s record;	-- iterator record for breweries
	h record;	-- iterator record for hops
	g record;	-- iterator record for grains
	e record;	-- iterator record for adjuncts
	ht text;	-- text var for hops
	gt text;	-- text var for grains
	et text; 	-- text var for adjuncts
	b BeerData;
begin
	for r in (
		select name
		from beers 
		where lower(name) like '%' || lower(partial_name) || '%'
	) loop

		b.beer := r.name;
		b.brewer := '';
		b.info := '';
		ht := '';
		gt := '';
		et := '';

		-- loop through breweries
		for s in (
			select
				breweries.name as brew_name
			from beers, brewed_by, breweries
			where beers.name = r.name
			and beers.id = brewed_by.beer
			and brewed_by.brewery = breweries.id
			order by 1
		) loop 

			if (b.brewer != '') then
				b.brewer := b.brewer || ' + ';	-- handle multiple breweries
			end if;
			b.brewer := b.brewer || s.brew_name;

		end loop;

		-- loop through hops
		for h in (
			select
				ingredients.name as ing_name
			from beers, contains, ingredients
			where beers.name = r.name
			and beers.id = contains.beer
			and contains.ingredient = ingredients.id
			and ingredients.itype = 'hop'
			order by 1
		) loop

			if (ht = '') then
				ht := ht || 'Hops: ';	-- handle multiple ingredients
			else 
				ht := ht || ',';
			end if;
			ht := ht || h.ing_name;

		end loop;

		b.info := b.info || ht;

		-- loop through grains
		for g in (
			select
				ingredients.name as ing_name
			from beers, contains, ingredients
			where beers.name = r.name
			and beers.id = contains.beer
			and contains.ingredient = ingredients.id
			and ingredients.itype = 'grain'
			order by 1
		) loop

			if (gt = '') then
				gt := gt || 'Grain: ';
			else 
				gt := gt || ',';
			end if;
			gt := gt || g.ing_name;

		end loop;

		if (g is not null and b.info != '') then
			b.info := b.info || e'\n';
		end if;
		b.info := b.info || gt;

		-- loop through adjuncts
		for e in (
			select
				ingredients.name as ing_name
			from beers, contains, ingredients
			where beers.name = r.name
			and beers.id = contains.beer
			and contains.ingredient = ingredients.id
			and ingredients.itype = 'adjunct'
			order by 1
		) loop

			if (et = '') then
				et := et || 'Extras: ';
			else 
				et := et || ',';
			end if;
			et := et || e.ing_name;

		end loop;

		if (e.ing_name != '' and b.info != '') then
			b.info := b.info || e'\n';
		end if;
		b.info := b.info || et;

		return next b;

	end loop;

	return;	-- if no beers match partial_name, return nothing

end;
$$ language plpgsql
;

