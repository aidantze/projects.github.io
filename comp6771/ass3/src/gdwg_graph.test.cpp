#include "gdwg_graph.h"

#include <catch2/catch.hpp>

#include <initializer_list>
#include <string>
#include <vector>

// Namespace storing all of the expected outputs for << operator, since including these in
// the tests themselves results in test cases more than 50 lines long and violates style
namespace {
	const auto expected_output1 = std::string_view(R"(1 (
)
2 (
)
3 (
)
)");

	const auto expected_output2 = std::string_view(R"(1 (
)
2 (
  2 -> 3 | U
)
3 (
)
)");

	const auto expected_output3 = std::string_view(R"(1 (
)
2 (
  2 -> 3 | W | 5
)
3 (
)
)");

	const auto expected_output4 = std::string_view(R"(1 (
  1 -> 2 | U
)
2 (
  2 -> 3 | U
)
3 (
  3 -> 1 | U
)
)");

	const auto expected_output5 = std::string_view(R"(1 (
)
2 (
  2 -> 1 | U
  2 -> 3 | U
)
3 (
)
)");

	const auto expected_output6 = std::string_view(R"(1 (
  1 -> 2 | W | 5
)
2 (
)
3 (
  3 -> 2 | W | 5
)
)");

	const auto expected_output7 = std::string_view(R"(1 (
)
2 (
  2 -> 3 | U
  2 -> 3 | W | 5
  2 -> 3 | W | 6
)
3 (
)
)");

	const auto expected_output8 = std::string_view(R"(1 (
  1 -> 1 | U
)
2 (
  2 -> 2 | W | 5
)
3 (
)
)");

	const auto expected_output9 = std::string_view(R"(1 (
)
2 (
  2 -> 1 | U
  2 -> 1 | W | 4
  2 -> 1 | W | 6
  2 -> 2 | U
  2 -> 3 | W | 5
)
3 (
)
)");
} // namespace

TEST_CASE("basic test") {
	// These are commented out right now
	//  because withour your implementation
	//  it will not compile. Uncomment them
	//  once you've done the work
	auto g = gdwg::Graph<int, std::string>{};
	auto n = 5;
	g.insert_node(n);
	CHECK(g.is_node(n));
}

// =============================================================================================
// GRAPH CONSTRUCTORS
// =============================================================================================
TEST_CASE("default constructor creates empty graph") {
	auto graph = gdwg::Graph<int, int>{};
	CHECK(graph.empty());
	CHECK(graph.nodes().empty());
}

TEST_CASE("constructor using empty vector creates empty graph") {
	auto input = std::vector<int>{};
	auto graph = gdwg::Graph<int, int>{input.begin(), input.end()};
	CHECK(graph.empty());
	CHECK(graph.nodes().empty());
}

TEST_CASE("constructor with iterators succeeds for ints") {
	auto input = std::vector<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input.begin(), input.end()};
	CHECK(graph.is_node(1));
	CHECK_FALSE(graph.is_node(0));
	CHECK_FALSE(graph.empty());

	CHECK(graph.nodes() == input);
	CHECK(graph.nodes().size() == 3);
}

TEST_CASE("constructor with iterators succeeds for strings") {
	auto input = std::vector<std::string>{"source", "wheres the lamb sauce", "worchestershire sauce"};
	auto graph = gdwg::Graph<std::string, int>{input.begin(), input.end()};
	CHECK(graph.is_node("wheres the lamb sauce"));
	CHECK_FALSE(graph.is_node("sauce"));
	CHECK_FALSE(graph.empty());

	CHECK(graph.nodes() == input);
	CHECK(graph.nodes().size() == 3);
}

TEST_CASE("constructor with initialiser list succeeds for ints") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	CHECK(graph.is_node(1));
	CHECK_FALSE(graph.is_node(0));
	CHECK_FALSE(graph.empty());

	CHECK(graph.nodes().at(0) == 1);
	CHECK(graph.nodes().size() == 3);
}

TEST_CASE("constructor with initialiser list succeeds for strings") {
	auto input = std::initializer_list<std::string>{"source", "wheres the lamb sauce", "worchestershire sauce"};
	auto graph = gdwg::Graph<std::string, int>{input};
	CHECK(graph.is_node("wheres the lamb sauce"));
	CHECK_FALSE(graph.is_node("sauce"));
	CHECK_FALSE(graph.empty());

	CHECK(graph.nodes().at(0) == "source");
	CHECK(graph.nodes().size() == 3);
}

// =============================================================================================
// EDGE CONSTRUCTORS, MEMBER FUNCTIONS AND OPERATORS
// =============================================================================================
TEST_CASE("unweighted edge constructor creates edge between int nodes with no weight") {
	auto edge = std::make_unique<gdwg::UnweightedEdge<int, int>>(1, 2);
	CHECK_FALSE(edge->is_weighted());

	auto nodes = edge->get_nodes();
	CHECK(nodes.first == 1);
	CHECK(nodes.second == 2);
}

TEST_CASE("unweighted edge constructor creates edge between string nodes with no weight") {
	auto edge = std::make_unique<gdwg::UnweightedEdge<std::string, std::string>>("abc", "def");
	CHECK_FALSE(edge->is_weighted());

	auto nodes = edge->get_nodes();
	CHECK(nodes.first == "abc");
	CHECK(nodes.second == "def");
}

TEST_CASE("weighted edge constructor for edge between int nodes succeeds for int weight") {
	auto edge = std::make_unique<gdwg::WeightedEdge<int, int>>(1, 2, 4);
	CHECK(edge->is_weighted());
	CHECK(edge->get_weight() == 4);

	auto nodes = edge->get_nodes();
	CHECK(nodes.first == 1);
	CHECK(nodes.second == 2);
}

TEST_CASE("weighted edge constructor for edge between string nodes succeeds for int weight") {
	auto edge = std::make_unique<gdwg::WeightedEdge<std::string, int>>("abc", "def", 4);
	CHECK(edge->is_weighted());
	CHECK(edge->get_weight() == 4);

	auto nodes = edge->get_nodes();
	CHECK(nodes.first == "abc");
	CHECK(nodes.second == "def");
}

TEST_CASE("weighted edge constructor for edge between int nodes succeeds for string weight") {
	auto edge = std::make_unique<gdwg::WeightedEdge<int, std::string>>(1, 2, "xyz");
	CHECK(edge->is_weighted());
	CHECK(edge->get_weight() == "xyz");

	auto nodes = edge->get_nodes();
	CHECK(nodes.first == 1);
	CHECK(nodes.second == 2);
}

TEST_CASE("weighted edge constructor for edge between string nodes succeeds for string weight") {
	auto edge = std::make_unique<gdwg::WeightedEdge<std::string, std::string>>("abc", "def", "xyz");
	CHECK(edge->is_weighted());
	CHECK(edge->get_weight() == "xyz");

	auto nodes = edge->get_nodes();
	CHECK(nodes.first == "abc");
	CHECK(nodes.second == "def");
}

TEST_CASE("unweighted edge constructor for self edge has no weight") {
	auto edge = std::make_unique<gdwg::UnweightedEdge<int, int>>(1, 1);
	CHECK_FALSE(edge->is_weighted());

	auto nodes = edge->get_nodes();
	CHECK(nodes.first == nodes.second);
}

TEST_CASE("weighted edge constructor for self edge succeeds for int weight") {
	auto edge = std::make_unique<gdwg::WeightedEdge<int, int>>(1, 1, 4);
	CHECK(edge->is_weighted());
	CHECK(edge->get_weight() == 4);

	auto nodes = edge->get_nodes();
	CHECK(nodes.first == nodes.second);
}

// =============================================================================================
// EDGE COMPARISON AND EXTRACTOR OPERATORS
// =============================================================================================

TEST_CASE("== operator compares unweighted edges succeeds") {
	auto edge1 = std::make_unique<gdwg::UnweightedEdge<int, int>>(1, 2);
	auto edge2 = std::make_unique<gdwg::UnweightedEdge<int, int>>(1, 2);

	CHECK(*edge1 == *edge2);
}

TEST_CASE("== operator compares weighted edges succeeds") {
	auto edge1 = std::make_unique<gdwg::WeightedEdge<int, int>>(1, 2, 4);
	auto edge2 = std::make_unique<gdwg::WeightedEdge<int, int>>(1, 2, 4);

	CHECK(*edge1 == *edge2);
}

TEST_CASE("== operator compares edges between different src nodes") {
	auto edge1 = std::make_unique<gdwg::UnweightedEdge<int, int>>(1, 2);
	auto edge2 = std::make_unique<gdwg::UnweightedEdge<int, int>>(3, 2);

	CHECK_FALSE(*edge1 == *edge2);
}

TEST_CASE("== operator compares edges between different dst nodes") {
	auto edge1 = std::make_unique<gdwg::UnweightedEdge<int, int>>(1, 2);
	auto edge2 = std::make_unique<gdwg::UnweightedEdge<int, int>>(1, 3);

	CHECK_FALSE(*edge1 == *edge2);
}

TEST_CASE("== operator compares unweighted and weighted edge") {
	auto edge1 = std::make_unique<gdwg::UnweightedEdge<int, int>>(1, 2);
	auto edge2 = std::make_unique<gdwg::WeightedEdge<int, int>>(1, 2, 4);

	CHECK_FALSE(*edge1 == *edge2);
}

TEST_CASE("== operator compares edges of different weights") {
	auto edge1 = std::make_unique<gdwg::WeightedEdge<int, int>>(1, 2, 5);
	auto edge2 = std::make_unique<gdwg::WeightedEdge<int, int>>(1, 2, 6);

	CHECK_FALSE(*edge1 == *edge2);
}

TEST_CASE("== operator compares edges of different directions") {
	auto edge1 = std::make_unique<gdwg::WeightedEdge<int, int>>(1, 2, 4);
	auto edge2 = std::make_unique<gdwg::WeightedEdge<int, int>>(2, 1, 4);

	CHECK_FALSE(*edge1 == *edge2);
}

TEST_CASE("== operator compares self edges succeeds") {
	auto edge1 = std::make_unique<gdwg::UnweightedEdge<int, int>>(1, 1);
	auto edge2 = std::make_unique<gdwg::UnweightedEdge<int, int>>(1, 1);

	CHECK(*edge1 == *edge2);
}

TEST_CASE("<< operator prints unweighted edge") {
	auto edge = std::make_unique<gdwg::UnweightedEdge<int, int>>(1, 2);

	auto oss = std::ostringstream{};
	oss << edge->print_edge();
	CHECK(oss.str() == "1 -> 2 | U");
}

TEST_CASE("<< operator prints weighted edge") {
	auto edge = std::make_unique<gdwg::WeightedEdge<int, int>>(1, 2, 4);

	auto oss = std::ostringstream{};
	oss << edge->print_edge();
	CHECK(oss.str() == "1 -> 2 | W | 4");
}

TEST_CASE("<< operator prints unweighted self edge") {
	auto edge = std::make_unique<gdwg::UnweightedEdge<int, int>>(1, 1);

	auto oss = std::ostringstream{};
	oss << edge->print_edge();
	CHECK(oss.str() == "1 -> 1 | U");
}

TEST_CASE("<< operator prints weighted self edge") {
	auto edge = std::make_unique<gdwg::WeightedEdge<int, int>>(1, 1, 4);

	auto oss = std::ostringstream{};
	oss << edge->print_edge();
	CHECK(oss.str() == "1 -> 1 | W | 4");
}

// =============================================================================================
// GRAPH ACCESSOR FUNCTIONS
// =============================================================================================

TEST_CASE("is_connected succeeds for single edge") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(1, 2);

	CHECK(graph.is_connected(1, 2));
}

TEST_CASE("is_connected succeeds for multiple edges") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(1, 2);
	graph.insert_edge(1, 2, 5);
	graph.insert_edge(1, 2, 6);

	CHECK(graph.is_connected(1, 2));
}

TEST_CASE("is_connected throws error when src or dst node does not exist") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	CHECK_THROWS_AS(graph.is_connected(0, 2), std::runtime_error);
	CHECK_THROWS_AS(graph.is_connected(1, 0), std::runtime_error);
}

TEST_CASE("is_connected fails when no edges exist") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(1, 2);
	graph.insert_edge(2, 3);
	CHECK_FALSE(graph.is_connected(2, 1));
}

TEST_CASE("is_connected succeeds for self edge") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(1, 1, 4);

	CHECK(graph.is_connected(1, 1));
}

TEST_CASE("edges succeeds for single edge") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	auto expected = std::make_unique<gdwg::UnweightedEdge<int, int>>(1, 2);

	graph.insert_edge(1, 2);

	auto edges = graph.edges(1, 2);
	CHECK(edges.size() == 1);
	CHECK(*edges.at(0) == *expected);
}

TEST_CASE("edges succeeds for multiple edges") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	auto expected1 = std::make_unique<gdwg::UnweightedEdge<int, int>>(1, 2);
	auto expected2 = std::make_unique<gdwg::WeightedEdge<int, int>>(1, 2, 5);
	auto expected3 = std::make_unique<gdwg::WeightedEdge<int, int>>(1, 2, 6);

	graph.insert_edge(1, 2);
	graph.insert_edge(1, 2, 5);
	graph.insert_edge(1, 2, 6);

	auto edges = graph.edges(1, 2);
	CHECK(edges.size() == 3);
	CHECK(*edges.at(0) == *expected1);
	CHECK(*edges.at(1) == *expected2);
	CHECK(*edges.at(2) == *expected3);
}

TEST_CASE("edges throws error when src or dst node does not exist") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	CHECK_THROWS_AS(graph.edges(0, 2), std::runtime_error);
	CHECK_THROWS_AS(graph.edges(1, 0), std::runtime_error);
}

TEST_CASE("edges is empty when no edges exist") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(1, 2);
	graph.insert_edge(2, 3, 5);

	auto edges = graph.edges(2, 1);
	CHECK(edges.size() == 0);
}

TEST_CASE("edges succeeds for self edge") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	auto expected = std::make_unique<gdwg::WeightedEdge<int, int>>(1, 1, 4);

	graph.insert_edge(1, 1, 4);

	auto edges = graph.edges(1, 1);
	CHECK(edges.size() == 1);
	CHECK(*edges.at(0) == *expected);
}

TEST_CASE("connections succeeds for single edge") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(1, 2);

	auto nodes = graph.connections(1);
	CHECK(nodes.size() == 1);
	CHECK(nodes.at(0) == 2);
}

TEST_CASE("connections succeeds for multiple edges") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(1, 2);
	graph.insert_edge(1, 3, 5);
	graph.insert_edge(2, 1, 6);

	auto nodes = graph.connections(1);
	CHECK(nodes.size() == 2);
	CHECK(nodes.at(0) == 2);
	CHECK(nodes.at(1) == 3);
}

TEST_CASE("connections throws error when src node does not exist") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	CHECK_THROWS_AS(graph.connections(0), std::runtime_error);
}

TEST_CASE("connections is empty when no edges exist") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(1, 2);
	graph.insert_edge(2, 3, 5);

	auto nodes = graph.connections(3);
	CHECK(nodes.size() == 0);
}

TEST_CASE("connections succeeds for self edge") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(1, 1, 4);

	auto nodes = graph.connections(1);
	CHECK(nodes.size() == 1);
	CHECK(nodes.at(0) == 1);
}

TEST_CASE("find succeeds for unweighted edge") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(1, 2);

	auto it = graph.find(1, 2);

	CHECK_FALSE(it == graph.end());
	CHECK((*it).from == 1);
	CHECK((*it).to == 2);
}

TEST_CASE("find succeeds for weighted edge") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(1, 2, 5);

	auto it = graph.find(1, 2, 5);

	CHECK_FALSE(it == graph.end());
	CHECK((*it).from == 1);
	CHECK((*it).to == 2);
	CHECK((*it).weight == 5);
}

TEST_CASE("find is end when no edges exist") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	auto it = graph.find(1, 2);

	CHECK(it == graph.end());
}

TEST_CASE("find is end when searching for unweighted edge that does not exist") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(1, 2, 5);

	auto it = graph.find(1, 2);

	CHECK(it == graph.end());
}

TEST_CASE("find is end when searching for weighted edge that does not exist") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(1, 2);

	auto it = graph.find(1, 2, 5);

	CHECK(it == graph.end());
}

TEST_CASE("find succeeds for self edge") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(1, 1, 5);

	auto it = graph.find(1, 1, 5);

	CHECK_FALSE(it == graph.end());
	CHECK((*it).from == 1);
	CHECK((*it).to == 1);
	CHECK((*it).weight == 5);
}

// =============================================================================================
// GRAPH MODIFIER FUNCTIONS
// =============================================================================================
TEST_CASE("insert node into empty int graph succeeds") {
	auto input = std::initializer_list<int>{};
	auto graph = gdwg::Graph<int, int>{input};
	CHECK(graph.empty());
	CHECK_FALSE(graph.is_node(4));

	CHECK(graph.insert_node(4));
	CHECK(graph.is_node(4));
	CHECK(graph.nodes().size() == 1);
}

TEST_CASE("insert node into int graph succeeds") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	CHECK(graph.nodes().size() == 3);
	CHECK_FALSE(graph.is_node(4));

	CHECK(graph.insert_node(4));
	CHECK(graph.is_node(4));
	CHECK(graph.nodes().size() == 4);
}

TEST_CASE("insert node into string graph succeeds") {
	auto input = std::initializer_list<std::string>{"abc", "def"};
	auto graph = gdwg::Graph<std::string, int>{input};
	CHECK(graph.nodes().size() == 2);
	CHECK_FALSE(graph.is_node("ghi"));

	CHECK(graph.insert_node("ghi"));
	CHECK(graph.is_node("ghi"));
	CHECK(graph.nodes().size() == 3);
}

TEST_CASE("insert node into graph which already contains the same node fails") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	CHECK(graph.nodes().size() == 3);
	CHECK(graph.is_node(1));

	CHECK_FALSE(graph.insert_node(1));
	CHECK(graph.nodes().size() == 3);
}

TEST_CASE("insert unweighted edge into int graph succeeds") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	auto expected = std::make_unique<gdwg::UnweightedEdge<int, int>>(1, 2);

	CHECK(graph.insert_edge(1, 2));
	CHECK(graph.is_connected(1, 2));

	auto edges = graph.edges(1, 2);
	CHECK(edges.size() == 1);
	CHECK(*edges.at(0) == *expected);
}

TEST_CASE("insert weighted edge into int graph succeeds") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, std::string>{input};
	auto expected = std::make_unique<gdwg::WeightedEdge<int, std::string>>(1, 2, "xyz");

	CHECK(graph.insert_edge(1, 2, "xyz"));
	CHECK(graph.is_connected(1, 2));

	auto edges = graph.edges(1, 2);
	CHECK(edges.size() == 1);
	CHECK(*edges.at(0) == *expected);
}

TEST_CASE("insert unweighted edge into string graph succeeds") {
	auto input = std::initializer_list<std::string>{"abc", "def", "ghi"};
	auto graph = gdwg::Graph<std::string, std::string>{input};
	auto expected = std::make_unique<gdwg::UnweightedEdge<std::string, std::string>>("abc", "def");

	CHECK(graph.insert_edge("abc", "def"));
	CHECK(graph.is_connected("abc", "def"));

	auto edges = graph.edges("abc", "def");
	CHECK(edges.size() == 1);
	CHECK(*edges.at(0) == *expected);
}

TEST_CASE("insert weighted edge into string graph succeeds") {
	auto input = std::initializer_list<std::string>{"abc", "def", "ghi"};
	auto graph = gdwg::Graph<std::string, int>{input};
	auto expected = std::make_unique<gdwg::WeightedEdge<std::string, int>>("abc", "def", 4);

	CHECK(graph.insert_edge("abc", "def", 4));
	CHECK(graph.is_connected("abc", "def"));

	auto edges = graph.edges("abc", "def");
	CHECK(edges.size() == 1);
	CHECK(*edges.at(0) == *expected);
}

TEST_CASE("insert unweighted edge into int graph throws when src node does not exist") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	CHECK_THROWS_AS(graph.insert_edge(0, 2), std::runtime_error);
}

TEST_CASE("insert weighted edge into int graph throws when dst node does not exist") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	CHECK_THROWS_AS(graph.insert_edge(1, 0, 4), std::runtime_error);
}

TEST_CASE("insert unweighted edge into int graph which already contains the same edge fails") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	CHECK(graph.insert_edge(1, 2));
	CHECK(graph.is_connected(1, 2));

	CHECK_FALSE(graph.insert_edge(1, 2));
}

TEST_CASE("insert weighted edge into int graph which already contains the same edge fails") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	CHECK(graph.insert_edge(1, 2, 4));
	CHECK(graph.is_connected(1, 2));

	CHECK_FALSE(graph.insert_edge(1, 2, 4));
}

TEST_CASE("insert unweighted edge into int graph which has a weighted edge succeeds") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	CHECK(graph.insert_edge(1, 2, 4));
	CHECK(graph.edges(1, 2).size() == 1);

	CHECK(graph.insert_edge(1, 2));
	CHECK(graph.edges(1, 2).size() == 2);
}

TEST_CASE("insert weighted edge into int graph which has an unweighted edge succeeds") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	CHECK(graph.insert_edge(1, 2));
	CHECK(graph.edges(1, 2).size() == 1);

	CHECK(graph.insert_edge(1, 2, 4));
	CHECK(graph.edges(1, 2).size() == 2);
}

TEST_CASE("insert weighted edge into int graph which has a different weighted edge succeeds") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	CHECK(graph.insert_edge(1, 2, 3));
	CHECK(graph.edges(1, 2).size() == 1);

	CHECK(graph.insert_edge(1, 2, 4));
	CHECK(graph.edges(1, 2).size() == 2);
}

TEST_CASE("replace node for int graph succeeds") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	CHECK(graph.is_node(3));
	CHECK_FALSE(graph.is_node(4));

	CHECK(graph.replace_node(3, 4));
	CHECK_FALSE(graph.is_node(3));
	CHECK(graph.is_node(4));
}

TEST_CASE("replace node throws when old_data node does not exist") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	CHECK_THROWS_AS(graph.replace_node(0, 4), std::runtime_error);
}

TEST_CASE("replace node returns false when new_data already exists") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	CHECK(graph.is_node(3));
	CHECK_FALSE(graph.is_node(4));

	CHECK_FALSE(graph.replace_node(3, 2));
	CHECK(graph.is_node(3));
	CHECK_FALSE(graph.is_node(4));
}

TEST_CASE("replace node updates single edge node to new node and preserves weight") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	auto expected = std::make_unique<gdwg::WeightedEdge<int, int>>(1, 4, 5);

	graph.insert_edge(1, 3, 5);

	CHECK(graph.replace_node(3, 4));
	CHECK_FALSE(graph.is_node(3));
	CHECK(graph.is_node(4));

	auto edges = graph.edges(1, 4);
	CHECK(edges.size() == 1);
	CHECK(*edges.at(0) == *expected);
}

TEST_CASE("replace node updates multiple edge nodes to new node") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	auto expected1 = std::make_unique<gdwg::UnweightedEdge<int, int>>(1, 4);
	auto expected2 = std::make_unique<gdwg::UnweightedEdge<int, int>>(4, 3);

	graph.insert_edge(1, 2);
	graph.insert_edge(2, 3);

	CHECK(graph.replace_node(2, 4));
	CHECK_FALSE(graph.is_node(2));
	CHECK(graph.is_node(4));

	auto edges = graph.edges(1, 4);
	CHECK(edges.size() == 1);
	CHECK(*edges.at(0) == *expected1);

	edges = graph.edges(4, 3);
	CHECK(edges.size() == 1);
	CHECK(*edges.at(0) == *expected2);
}

TEST_CASE("replace node updates self edge node to new node") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	auto expected = std::make_unique<gdwg::WeightedEdge<int, int>>(4, 4, 5);

	graph.insert_edge(1, 1, 5);

	CHECK(graph.replace_node(1, 4));
	CHECK_FALSE(graph.is_node(1));
	CHECK(graph.is_node(4));

	auto edges = graph.edges(4, 4);
	CHECK(edges.size() == 1);
	CHECK(*edges.at(0) == *expected);
}

TEST_CASE("merge replace node for int graph succeeds") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	CHECK(graph.is_node(1));
	CHECK(graph.is_node(2));

	graph.merge_replace_node(1, 2);
	CHECK_FALSE(graph.is_node(1));
	CHECK(graph.is_node(2));
}

TEST_CASE("merge replace node throws when old_data node does not exist") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	CHECK_THROWS_AS(graph.merge_replace_node(0, 2), std::runtime_error);
}

TEST_CASE("merge replace node migrates single edge to new node and preserves weight") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	auto expected = std::make_unique<gdwg::WeightedEdge<int, int>>(2, 3, 5);

	graph.insert_edge(1, 3, 5);

	graph.merge_replace_node(1, 2);
	CHECK_FALSE(graph.is_node(1));
	CHECK(graph.is_node(2));

	auto edges = graph.edges(2, 3);
	CHECK(edges.size() == 1);
	CHECK(*edges.at(0) == *expected);
}

TEST_CASE("merge replace node migrates multiple edges to new node") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	auto expected1 = std::make_unique<gdwg::UnweightedEdge<int, int>>(1, 3);
	auto expected2 = std::make_unique<gdwg::UnweightedEdge<int, int>>(3, 1);

	graph.insert_edge(1, 2);
	graph.insert_edge(2, 1);

	graph.merge_replace_node(2, 3);
	CHECK_FALSE(graph.is_node(2));
	CHECK(graph.is_node(3));

	auto edges = graph.edges(1, 3);
	CHECK(edges.size() == 1);
	CHECK(*edges.at(0) == *expected1);

	edges = graph.edges(3, 1);
	CHECK(edges.size() == 1);
	CHECK(*edges.at(0) == *expected2);
}

TEST_CASE("merge replace node ignores duplicate edge when migrating to new node") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	auto expected = std::make_unique<gdwg::UnweightedEdge<int, int>>(1, 3);

	graph.insert_edge(1, 2);
	graph.insert_edge(1, 3);

	graph.merge_replace_node(2, 3);
	CHECK_FALSE(graph.is_node(2));
	CHECK(graph.is_node(3));

	auto edges = graph.edges(1, 3);
	CHECK(edges.size() == 1);
	CHECK(*edges.at(0) == *expected);
}

TEST_CASE("merge replace node creates self edge when migrating to new node") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	auto expected = std::make_unique<gdwg::UnweightedEdge<int, int>>(1, 1);

	graph.insert_edge(1, 2);

	graph.merge_replace_node(2, 1);
	CHECK_FALSE(graph.is_node(2));
	CHECK(graph.is_node(1));

	auto edges = graph.edges(1, 1);
	CHECK(edges.size() == 1);
	CHECK(*edges.at(0) == *expected);
}

TEST_CASE("merge replace node migrates entire self edge to new node") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	auto expected = std::make_unique<gdwg::UnweightedEdge<int, int>>(3, 3);

	graph.insert_edge(2, 2);

	graph.merge_replace_node(2, 3);
	CHECK_FALSE(graph.is_node(2));
	CHECK(graph.is_node(3));

	auto edges = graph.edges(3, 3);
	CHECK(edges.size() == 1);
	CHECK(*edges.at(0) == *expected);
}

TEST_CASE("merge replace node ignores duplicate self edge when migrating new node") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	auto expected = std::make_unique<gdwg::UnweightedEdge<int, int>>(3, 3);

	graph.insert_edge(2, 3);
	graph.insert_edge(3, 3);

	graph.merge_replace_node(2, 3);
	CHECK_FALSE(graph.is_node(2));
	CHECK(graph.is_node(3));

	auto edges = graph.edges(3, 3);
	CHECK(edges.size() == 1);
	CHECK(*edges.at(0) == *expected);
}

TEST_CASE("erase node for int graph succeeds") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	CHECK(graph.erase_node(2));
	CHECK_FALSE(graph.is_node(2));
}

TEST_CASE("erase node for int graph where node does not exist fails") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	CHECK_FALSE(graph.erase_node(4));
}

TEST_CASE("erase node for int graph removes all edges outgoing from node") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	graph.insert_edge(2, 3);

	CHECK(graph.erase_node(2));
	CHECK_FALSE(graph.is_node(2));

	CHECK_THROWS_AS(graph.is_connected(2, 3), std::runtime_error);
}

TEST_CASE("erase node for int graph removes all edges incoming to node") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	graph.insert_edge(2, 3);

	CHECK(graph.erase_node(3));
	CHECK_FALSE(graph.is_node(3));

	CHECK_THROWS_AS(graph.is_connected(2, 3), std::runtime_error);
}

TEST_CASE("erase node for int graph removes self edge around the node") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	graph.insert_edge(2, 2);

	CHECK(graph.erase_node(2));
	CHECK_FALSE(graph.is_node(2));

	CHECK_THROWS_AS(graph.is_connected(2, 2), std::runtime_error);
}

TEST_CASE("erase unweighted edge for int graph succeeds") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(2, 3);

	CHECK(graph.erase_edge(2, 3));

	CHECK(graph.edges(2, 3).size() == 0);
}

TEST_CASE("erase weighted edge for int graph succeeds") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(2, 3, 5);

	CHECK(graph.erase_edge(2, 3, 5));

	CHECK(graph.edges(2, 3).size() == 0);
}

TEST_CASE("erase unweighted edge for int graph throws when src does not exist") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	auto expected = std::make_unique<gdwg::UnweightedEdge<int, int>>(2, 3);

	graph.insert_edge(2, 3);

	CHECK_THROWS_AS(graph.erase_edge(4, 3), std::runtime_error);

	auto edges = graph.edges(2, 3);
	CHECK(edges.size() == 1);
	CHECK(*edges.at(0) == *expected);
}

TEST_CASE("erase weighted edge for int graph throws when dst does not exist") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	auto expected = std::make_unique<gdwg::WeightedEdge<int, int>>(2, 3, 5);

	graph.insert_edge(2, 3, 5);

	CHECK_THROWS_AS(graph.erase_edge(2, 4, 5), std::runtime_error);

	auto edges = graph.edges(2, 3);
	CHECK(edges.size() == 1);
	CHECK(*edges.at(0) == *expected);
}

TEST_CASE("erase unweighted edge for int graph fails when no edge exists") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	auto expected = std::make_unique<gdwg::UnweightedEdge<int, int>>(2, 3);

	graph.insert_edge(2, 3);

	CHECK_FALSE(graph.erase_edge(2, 1));
	CHECK_FALSE(graph.erase_edge(1, 3));

	auto edges = graph.edges(2, 3);
	CHECK(edges.size() == 1);
	CHECK(*edges.at(0) == *expected);
}

TEST_CASE("erase weighted edge for int graph fails when no edge exists") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	auto expected = std::make_unique<gdwg::WeightedEdge<int, int>>(2, 3, 5);

	graph.insert_edge(2, 3, 5);

	CHECK_FALSE(graph.erase_edge(2, 1, 5));
	CHECK_FALSE(graph.erase_edge(1, 3, 5));

	auto edges = graph.edges(2, 3);
	CHECK(edges.size() == 1);
	CHECK(*edges.at(0) == *expected);
}

TEST_CASE("erase edge for int graph fails if weights are different") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	auto expected1 = std::make_unique<gdwg::WeightedEdge<int, int>>(2, 3, 2);
	auto expected2 = std::make_unique<gdwg::WeightedEdge<int, int>>(2, 3, 5);

	graph.insert_edge(2, 3, 5);
	graph.insert_edge(2, 3, 2);

	CHECK_FALSE(graph.erase_edge(2, 3));
	CHECK_FALSE(graph.erase_edge(2, 3, 6));

	auto edges = graph.edges(2, 3);
	CHECK(edges.size() == 2);
	CHECK(*edges.at(0) == *expected1);
	CHECK(*edges.at(1) == *expected2);
}

TEST_CASE("erase edge for int graph succeeds for self edges") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(2, 2);
	graph.insert_edge(2, 2, 5);

	CHECK(graph.erase_edge(2, 2, 5));
	CHECK(graph.erase_edge(2, 2));

	CHECK(graph.edges(2, 3).size() == 0);
}

// TODO: add tests for erase_edge for iterators

TEST_CASE("clear clears an empty graph") {
	auto graph = gdwg::Graph<int, int>{};

	CHECK_NOTHROW(graph.clear());

	CHECK(graph.empty());
}

TEST_CASE("clear clears a graph with many nodes but no edges") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.clear();

	CHECK(graph.empty());
}

TEST_CASE("clear clears a graph with many nodes and edges") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(2, 3);
	graph.insert_edge(2, 3, 5);

	graph.clear();

	CHECK(graph.empty());

	CHECK_THROWS_AS(graph.edges(2, 3), std::runtime_error);
}

// =============================================================================================
// GRAPH COMPARISON AND EXTRACTOR OPERATORS
// =============================================================================================

TEST_CASE("== operator compares empty graphs") {
	auto graph1 = gdwg::Graph<int, int>{};
	auto graph2 = gdwg::Graph<int, int>{};

	CHECK(graph1 == graph2);
}

TEST_CASE("== operator compares graphs with many nodes but no edges") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph1 = gdwg::Graph<int, int>{input};
	auto graph2 = gdwg::Graph<int, int>{input};

	CHECK(graph1 == graph2);
}

TEST_CASE("== operator compares graphs with many nodes and edges") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph1 = gdwg::Graph<int, int>{input};
	auto graph2 = gdwg::Graph<int, int>{input};

	graph1.insert_edge(2, 3);
	graph1.insert_edge(3, 2, 5);
	graph2.insert_edge(2, 3);
	graph2.insert_edge(3, 2, 5);

	CHECK(graph1 == graph2);
}

TEST_CASE("== operator compares graphs of different node orders succeeds") {
	auto input1 = std::initializer_list<int>{1, 2, 3};
	auto input2 = std::initializer_list<int>{3, 2, 1};
	auto graph1 = gdwg::Graph<int, int>{input1};
	auto graph2 = gdwg::Graph<int, int>{input2};

	CHECK(graph1 == graph2);
}

TEST_CASE("== operator compares graphs of different edge orders succeeds") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph1 = gdwg::Graph<int, int>{input};
	auto graph2 = gdwg::Graph<int, int>{input};

	graph1.insert_edge(2, 3);
	graph1.insert_edge(3, 2, 5);
	graph2.insert_edge(3, 2, 5);
	graph2.insert_edge(2, 3);

	CHECK(graph1 == graph2);
}

TEST_CASE("== operator fails if graphs have different number of nodes") {
	auto input1 = std::initializer_list<int>{1, 2, 3};
	auto input2 = std::initializer_list<int>{1, 2};
	auto graph1 = gdwg::Graph<int, int>{input1};
	auto graph2 = gdwg::Graph<int, int>{input2};

	CHECK_FALSE(graph1 == graph2);
}

TEST_CASE("== operator fails if graphs have same edge of different directions") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph1 = gdwg::Graph<int, int>{input};
	auto graph2 = gdwg::Graph<int, int>{input};

	graph1.insert_edge(2, 3);
	graph2.insert_edge(3, 2);

	CHECK_FALSE(graph1 == graph2);
}

TEST_CASE("<< operator prints empty graph") {
	auto graph = gdwg::Graph<int, int>{};
	const auto expected = std::string_view("");

	auto oss = std::ostringstream{};
	oss << graph;
	CHECK(oss.str() == expected);
}

TEST_CASE("<< operator prints int graph with many nodes but no edges") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	auto oss = std::ostringstream{};
	oss << graph;
	CHECK(oss.str() == expected_output1);
}

TEST_CASE("<< operator prints int graph with single unweighted edge") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(2, 3);

	auto oss = std::ostringstream{};
	oss << graph;
	CHECK(oss.str() == expected_output2);
}

TEST_CASE("<< operator prints int graph with single weighted edge") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(2, 3, 5);

	auto oss = std::ostringstream{};
	oss << graph;
	CHECK(oss.str() == expected_output3);
}

TEST_CASE("<< operator prints int graph with multiple edges between different nodes") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(2, 3);
	graph.insert_edge(1, 2);
	graph.insert_edge(3, 1);

	auto oss = std::ostringstream{};
	oss << graph;
	CHECK(oss.str() == expected_output4);
}

TEST_CASE("<< operator prints int graph with multiple edges for same src node") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(2, 1);
	graph.insert_edge(2, 3);

	auto oss = std::ostringstream{};
	oss << graph;
	CHECK(oss.str() == expected_output5);
}

TEST_CASE("<< operator prints int graph with multiple edges for same dst node") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(3, 2, 5);
	graph.insert_edge(1, 2, 5);

	auto oss = std::ostringstream{};
	oss << graph;
	CHECK(oss.str() == expected_output6);
}

TEST_CASE("<< operator prints int graph with multiple edges of different weights") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(2, 3, 6);
	graph.insert_edge(2, 3, 5);
	graph.insert_edge(2, 3);

	auto oss = std::ostringstream{};
	oss << graph;
	CHECK(oss.str() == expected_output7);
}

TEST_CASE("<< operator prints int graph with self edges") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(1, 1);
	graph.insert_edge(2, 2, 5);

	auto oss = std::ostringstream{};
	oss << graph;
	CHECK(oss.str() == expected_output8);
}

TEST_CASE("<< operator prints int graph where all edges are in correct order") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(2, 2);
	graph.insert_edge(2, 1, 4);
	graph.insert_edge(2, 3, 5);
	graph.insert_edge(2, 1, 6);
	graph.insert_edge(2, 1);

	auto oss = std::ostringstream{};
	oss << graph;
	CHECK(oss.str() == expected_output9);
}

// =============================================================================================
// GRAPH ITERATORS AND ITERATOR ACCESS
// =============================================================================================
TEST_CASE("iterator equal begin and end for empty graph") {
	auto graph = gdwg::Graph<int, int>{};
	CHECK(graph.begin() == graph.end());
}

TEST_CASE("iterator equal begin and end for int graph with no edges") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};
	CHECK(graph.begin() == graph.end());
}

TEST_CASE("iterator begin and end for graph with unweighted edges") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(1, 2);
	graph.insert_edge(2, 3);
	graph.insert_edge(1, 3);
	graph.insert_edge(2, 1);

	CHECK_FALSE(graph.begin() == graph.end());

	auto it = graph.begin();
	CHECK((*it).from == 1);
	CHECK((*it).to == 2);
	CHECK_FALSE((*it).weight.has_value());

	++it;
	CHECK((*it).from == 2);
	CHECK((*it).to == 3);

	++it;
	CHECK((*it).from == 1);
	CHECK((*it).to == 3);

	++it;
	CHECK((*it).from == 2);
	CHECK((*it).to == 1);

	++it;
	CHECK(it == graph.end());

	--it;
	CHECK((*it).from == 2);
	CHECK((*it).to == 1);

	--it;
	--it;
	--it;
	CHECK(it == graph.begin());
	CHECK((*it).from == 1);
	CHECK((*it).to == 2);
}

TEST_CASE("iterator begin and end for graph with weighted edges") {
	auto input = std::initializer_list<int>{1, 2, 3};
	auto graph = gdwg::Graph<int, int>{input};

	graph.insert_edge(1, 2, 4);
	graph.insert_edge(2, 3, 5);
	graph.insert_edge(1, 3, 6);
	graph.insert_edge(2, 1);

	CHECK_FALSE(graph.begin() == graph.end());

	auto it = graph.begin();
	CHECK((*it).from == 1);
	CHECK((*it).to == 2);
	CHECK((*it).weight.value() == 4);

	++it;
	CHECK((*it).from == 2);
	CHECK((*it).to == 3);
	CHECK((*it).weight.value() == 5);

	++it;
	CHECK((*it).from == 1);
	CHECK((*it).to == 3);
	CHECK((*it).weight.value() == 6);

	++it;
	CHECK((*it).from == 2);
	CHECK((*it).to == 1);
	CHECK_FALSE((*it).weight.has_value());

	++it;
	CHECK(it == graph.end());

	--it;
	CHECK((*it).from == 2);
	CHECK((*it).to == 1);

	--it;
	--it;
	--it;
	CHECK(it == graph.begin());
	CHECK((*it).from == 1);
	CHECK((*it).to == 2);
}
