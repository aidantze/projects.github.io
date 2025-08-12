#include "./filtered_string_view.h"

#include <catch2/catch.hpp>
#include <compare>
#include <string>
#include <vector>

namespace {
    auto vowels_only = [](const char& c) {
        auto lower = static_cast<char>(std::tolower(static_cast<unsigned char>(c)));
        return lower == 'a' || lower == 'e' || lower == 'i' || lower == 'o' || lower == 'u';
    };

    auto always_false = [](const char&) { return false; };
}

// =============================================================================================
// PREDICATES
// =============================================================================================
TEST_CASE("default predicate always returns true") {
    auto pred = fsv::filtered_string_view::default_predicate;
    CHECK(pred('a'));
    CHECK(pred('Z'));
    CHECK(pred('0'));
    CHECK(pred('?'));
    CHECK(pred(' '));
}

TEST_CASE("always_false predicate always returns false") {
    CHECK_FALSE(always_false('a'));
    CHECK_FALSE(always_false('Z'));
    CHECK_FALSE(always_false('0'));
    CHECK_FALSE(always_false('?'));
    CHECK_FALSE(always_false(' '));
}

TEST_CASE("vowels_only predicate behaves as expected") {
    // test case for the helper predicate above
    CHECK(vowels_only('a'));
    CHECK(vowels_only('E'));
    CHECK(vowels_only('i'));
    CHECK(vowels_only('O'));
    CHECK(vowels_only('u'));

    CHECK_FALSE(vowels_only('z'));
    CHECK_FALSE(vowels_only('Y'));
    CHECK_FALSE(vowels_only('0'));
    CHECK_FALSE(vowels_only('?'));
    CHECK_FALSE(vowels_only(' '));
}

// =============================================================================================
// CONSTRUCTORS AND MEMBER FUNCTIONS
// =============================================================================================
TEST_CASE("default constructor creates empty view") {
    auto view = fsv::filtered_string_view{};
    CHECK(view.data() == nullptr);
    CHECK(view.empty());

    for (auto c : {'a', 'Z', '0', '?', ' '}) {
        CHECK(view.predicate()(c) == fsv::filtered_string_view::default_predicate(c));
    }
}

TEST_CASE("constructor with std::string using default predicate") {
    auto test = std::string{"abc"};
    auto view = fsv::filtered_string_view{test};
    CHECK(view.data() == test.data());
    CHECK(view.size() == 3);
    CHECK_FALSE(view.empty());

    for (auto c : {'a', 'Z', '0', '?', ' '}) {
        CHECK(view.predicate()(c) == fsv::filtered_string_view::default_predicate(c));
    }
}

TEST_CASE("constructor with std::string using custom predicate") {
    auto test = std::string{"abc"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    CHECK(view.data() == test.data());
    CHECK(view.size() == 1);
    CHECK_FALSE(view.empty());

    for (auto c : {'a', 'Z', '0', '?', ' '}) {
        CHECK(view.predicate()(c) == vowels_only(c));
    }
}

TEST_CASE("constructor with const char* using default predicate") {
    auto test = "abc";
    auto view = fsv::filtered_string_view{test};
    CHECK(view.data() == test);
    CHECK(view.size() == 3);

    for (auto c : {'a', 'Z', '0', '?', ' '}) {
        CHECK(view.predicate()(c) == fsv::filtered_string_view::default_predicate(c));
    }
}

TEST_CASE("constructor with const char* using custom predicate") {
    auto test = "abc";
    auto view = fsv::filtered_string_view{test, vowels_only};
    CHECK(view.data() == test);
    CHECK(view.size() == 1);

    for (auto c : {'a', 'Z', '0', '?', ' '}) {
        CHECK(view.predicate()(c) == vowels_only(c));
    }
}

TEST_CASE("constructor with std::string does not satisfy custom predicate") {
    auto test = std::string{"sdfghjkl"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    CHECK(view.data() == test.data()); // basic pointer should match
    CHECK(view.size() == 0);
    CHECK(view.empty());

    for (auto c : {'a', 'Z', '0', '?', ' '}) {
        CHECK(view.predicate()(c) == vowels_only(c));
    }
}

TEST_CASE("copy constructor using default predicate") {
    auto test = std::string{"copy test"};
    auto view = fsv::filtered_string_view{test};
    const auto copy = view;

    CHECK(copy.data() == view.data());
    CHECK(copy.size() == 9);

    for (auto c : {'a', 'Z', '0', '?', ' '}) {
        CHECK(view.predicate()(c) == fsv::filtered_string_view::default_predicate(c));
        CHECK(copy.predicate()(c) == fsv::filtered_string_view::default_predicate(c));
    }
}

TEST_CASE("copy constructor using custom predicate") {
    auto test = std::string{"copy test"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    const auto copy = view;

    CHECK(copy.data() == view.data());
    CHECK(copy.size() == 2);

    for (auto c : {'a', 'Z', '0', '?', ' '}) {
        CHECK(view.predicate()(c) == vowels_only(c));
        CHECK(copy.predicate()(c) == vowels_only(c));
    }
}

TEST_CASE("move constructor using default predicate") {
    auto test = std::string{"copy test"};
    auto view = fsv::filtered_string_view{test};
    auto orig_data = view.data();

    const auto move = std::move(view);
    CHECK(view.data() == nullptr);
    CHECK(move.data() == orig_data);

    for (auto c : {'a', 'Z', '0', '?', ' '}) {
        CHECK(view.predicate()(c) == fsv::filtered_string_view::default_predicate(c));
        CHECK(move.predicate()(c) == fsv::filtered_string_view::default_predicate(c));
    }
}

TEST_CASE("move constructor using custom predicate") {
    auto test = std::string{"copy test"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    auto orig_data = view.data();

    const auto move = std::move(view);
    CHECK(view.data() == nullptr);
    CHECK(move.data() == orig_data);

    for (auto c : {'a', 'Z', '0', '?', ' '}) {
        CHECK(view.predicate()(c) == fsv::filtered_string_view::default_predicate(c));
        CHECK(move.predicate()(c) == vowels_only(c));
    }
}

// =============================================================================================
// MEMBER FUNCTIONS AND MEMBER OPERATORS
// =============================================================================================
TEST_CASE("at and subscript for empty view") {
    auto view = fsv::filtered_string_view{};
    CHECK(view.empty());

    CHECK_THROWS_AS(view.at(0), std::domain_error);
}

TEST_CASE("at and subscript for index out of bounds using default predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test};
    CHECK(view.data() == test.data());

    CHECK_THROWS_AS(view.at(6), std::domain_error);
    CHECK_NOTHROW(view[6]);
}

TEST_CASE("at and subscript for index out of bounds using custom predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    CHECK(view.data() == test.data());

    CHECK_THROWS_AS(view.at(2), std::domain_error);
    CHECK_NOTHROW(view[2]);
}

TEST_CASE("at and subscript for valid view using default predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test};
    CHECK(view.data() == test.data());

    CHECK(view.at(0) == 'a');
    CHECK(view[0] == 'a');
    CHECK(view.at(1) == 'b');
    CHECK(view[1] == 'b');
}

TEST_CASE("at and subscript for valid view using custom predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    CHECK(view.data() == test.data());

    CHECK(view.at(0) == 'a');
    CHECK(view[0] == 'a');
    CHECK(view.at(1) == 'e');
    CHECK(view[1] == 'e');
}

TEST_CASE("copy assignment using default predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test};
    auto copy = fsv::filtered_string_view{};
    copy = view;
    CHECK(copy.data() == view.data());
    CHECK(copy.at(0) == view.at(0));
    CHECK(copy[0] == view[0]);

    CHECK_THROWS_AS(copy.at(6), std::domain_error);
    CHECK_NOTHROW(copy[6]);
}

TEST_CASE("copy assignment using custom predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    auto copy = fsv::filtered_string_view{};
    copy = view;
    CHECK(copy.data() == view.data());
    CHECK(copy.at(0) == view.at(0));
    CHECK(copy[0] == view[0]);

    CHECK_THROWS_AS(copy.at(2), std::domain_error);
    CHECK_NOTHROW(copy[2]);
}

TEST_CASE("move assignment using default predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test};
    auto move = fsv::filtered_string_view{};
    move = std::move(view);
    CHECK(move.data() == test.data());
    CHECK(move.at(0) == 'a');
    CHECK(move[0] == 'a');

    CHECK(view.data() == nullptr);
    CHECK(view.size() == 0);
    CHECK_THROWS_AS(view.at(0), std::domain_error);
    CHECK_NOTHROW(view[0]);
}

TEST_CASE("move assignment using custom predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    auto move = fsv::filtered_string_view{};
    move = std::move(view);
    CHECK(move.data() == test.data());
    CHECK(move.at(0) == 'a');
    CHECK(move[0] == 'a');

    CHECK(view.data() == nullptr);
    CHECK(view.size() == 0);
    CHECK_THROWS_AS(view.at(0), std::domain_error);
    CHECK_NOTHROW(view[0]);
}

TEST_CASE("std::string conversion using default predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test};
    auto str = static_cast<std::string>(view);
    CHECK(view.data() == test.data());
    CHECK(str == test);
}

TEST_CASE("std::string conversion using custom predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    auto str = static_cast<std::string>(view);
    CHECK(view.data() == test.data());
    CHECK(str == "ae");
}

TEST_CASE("std::string conversion does not satisfy custom predicate") {
    auto test = std::string{"sdfghjkl"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    auto str = static_cast<std::string>(view);
    CHECK(view.data() == test.data());
    CHECK(str.empty());
}

// =============================================================================================
// NON-MEMBER OPERATORS
// =============================================================================================
TEST_CASE("<=> operator for default constructor compares equal to itself") {
    auto view = fsv::filtered_string_view{};
    CHECK((view <=> view) == std::strong_ordering::equal);
}

TEST_CASE("<=> operator equality comparison for two views using default predicate") {
    auto view1 = fsv::filtered_string_view{"abc"};
    auto view2 = fsv::filtered_string_view{"abc"};
    CHECK((view1 <=> view2) == std::strong_ordering::equal);
}

TEST_CASE("<=> operator equality comparison for two views using custom predicate") {
    auto view1 = fsv::filtered_string_view{"abcde", vowels_only};
    auto view2 = fsv::filtered_string_view{"afghjkle", vowels_only};
    CHECK((view1 <=> view2) == std::strong_ordering::equal);
}

TEST_CASE("<=> operator equality comparison for two views that don't satisfy predicate") {
    auto view1 = fsv::filtered_string_view{"sfghjkl", vowels_only};
    auto view2 = fsv::filtered_string_view{"qwrtyzxcvbnm", vowels_only};
    CHECK((view1 <=> view2) == std::strong_ordering::equal);
}

TEST_CASE("<=> operator equality comparison for two views of different predicates") {
    auto view1 = fsv::filtered_string_view{"aeiou"};
    auto view2 = fsv::filtered_string_view{"aeiou", vowels_only};
    CHECK((view1 <=> view2) == std::strong_ordering::equal);
}

TEST_CASE("<=> operator inequality comparison for two views using default predicate") {
    auto view1 = fsv::filtered_string_view{"abc"};
    auto view2 = fsv::filtered_string_view{"def"};
    CHECK((view1 <=> view2) != std::strong_ordering::equal);
}

TEST_CASE("<=> operator inequality comparison for two views using custom predicate") {
    auto view1 = fsv::filtered_string_view{"abc", vowels_only};
    auto view2 = fsv::filtered_string_view{"def", vowels_only};
    CHECK((view1 <=> view2) != std::strong_ordering::equal);
}

TEST_CASE("<=> operator inequality comparison for two views, one not satisfying predicate") {
    auto view1 = fsv::filtered_string_view{"abcde", vowels_only};
    auto view2 = fsv::filtered_string_view{"sfghjkl", vowels_only};
    CHECK((view1 <=> view2) != std::strong_ordering::equal);
}

TEST_CASE("<=> operator inequality comparison for two views of different predicates") {
    auto view1 = fsv::filtered_string_view{"abcde"};
    auto view2 = fsv::filtered_string_view{"abcde", vowels_only};
    CHECK((view1 <=> view2) != std::strong_ordering::equal);
}

TEST_CASE("<=> operator greater relational comparison for two views using default predicate") {
    auto view1 = fsv::filtered_string_view{"abc"};
    auto view2 = fsv::filtered_string_view{"aaa"};
    CHECK((view1 <=> view2) == std::strong_ordering::greater);
    CHECK((view1 <=> view2) != std::strong_ordering::less);
}

TEST_CASE("<=> operator lesser relational comparison for two views using default predicate") {
    auto view1 = fsv::filtered_string_view{"abc"};
    auto view2 = fsv::filtered_string_view{"def"};
    CHECK((view1 <=> view2) == std::strong_ordering::less);
    CHECK((view1 <=> view2) != std::strong_ordering::greater);
}

TEST_CASE("<=> operator greater relational comparison for two views with same predicate") {
    auto view1 = fsv::filtered_string_view{"aaa", vowels_only};
    auto view2 = fsv::filtered_string_view{"abc", vowels_only};
    CHECK((view1 <=> view2) == std::strong_ordering::greater);
    CHECK((view1 <=> view2) != std::strong_ordering::less);
}

TEST_CASE("<=> operator lesser relational comparison for two views with same predicate") {
    auto view1 = fsv::filtered_string_view{"abc", vowels_only};
    auto view2 = fsv::filtered_string_view{"def", vowels_only};
    CHECK((view1 <=> view2) == std::strong_ordering::less);
    CHECK((view1 <=> view2) != std::strong_ordering::greater);
}

TEST_CASE("<=> operator greater relational comparison for two views of different predicates") {
    auto view1 = fsv::filtered_string_view{"abcde", vowels_only};
    auto view2 = fsv::filtered_string_view{"abcde"};
    CHECK((view1 <=> view2) == std::strong_ordering::greater);
    CHECK((view1 <=> view2) != std::strong_ordering::less);
}

TEST_CASE("<=> operator lesser relational comparison for two views of different predicates") {
    auto view1 = fsv::filtered_string_view{"abcde"};
    auto view2 = fsv::filtered_string_view{"abcde", vowels_only};
    CHECK((view1 <=> view2) == std::strong_ordering::less);
    CHECK((view1 <=> view2) != std::strong_ordering::greater);
}

TEST_CASE("== operator for two default constructed views") {
    auto view1 = fsv::filtered_string_view{};
    auto view2 = fsv::filtered_string_view{};
    CHECK(view1 == view2);
}

TEST_CASE("== operator for two views using default predicate") {
    auto view1 = fsv::filtered_string_view{"abc"};
    auto view2 = fsv::filtered_string_view{"abc"};
    CHECK(view1 == view2);
}

TEST_CASE("== operator for two views using custom predicate") {
    auto view1 = fsv::filtered_string_view{"abcde", vowels_only};
    auto view2 = fsv::filtered_string_view{"afghjkle", vowels_only};
    CHECK(view1 == view2);
}

TEST_CASE("== operator for two views of different predicates") {
    auto view1 = fsv::filtered_string_view{"aeiou"};
    auto view2 = fsv::filtered_string_view{"aeiou", vowels_only};
    CHECK(view1 == view2);
}

TEST_CASE("!= operator for two views using default predicate") {
    auto view1 = fsv::filtered_string_view{"abcde"};
    auto view2 = fsv::filtered_string_view{"sfghjkl"};
    CHECK(view1 != view2);
}

TEST_CASE("!= operator for two views using custom predicate") {
    auto view1 = fsv::filtered_string_view{"abcde", vowels_only};
    auto view2 = fsv::filtered_string_view{"sfghjkl", vowels_only};
    CHECK(view1 != view2);
}

TEST_CASE("!= operator for two views of different predicates") {
    auto view1 = fsv::filtered_string_view{"sfghjkl"};
    auto view2 = fsv::filtered_string_view{"sfghjkl", vowels_only};
    CHECK(view1 != view2);
}

TEST_CASE("> and >= operators for two views using default predicate") {
    auto view1 = fsv::filtered_string_view{"abc"};
    auto view2 = fsv::filtered_string_view{"aaa"};
    CHECK(view1 >= view2);
    CHECK(view1 > view2);
}

TEST_CASE("> and >= operators for two views using custom predicate") {
    auto view1 = fsv::filtered_string_view{"aaa", vowels_only};
    auto view2 = fsv::filtered_string_view{"abc", vowels_only};
    CHECK(view1 >= view2);
    CHECK(view1 > view2);
}

TEST_CASE("> and >= operators for two views of different predicates") {
    auto view1 = fsv::filtered_string_view{"abcde", vowels_only};
    auto view2 = fsv::filtered_string_view{"abcde"};
    CHECK(view1 >= view2);
    CHECK(view1 > view2);
}

TEST_CASE("< and <= operators for two views using default predicate") {
    auto view1 = fsv::filtered_string_view{"aaa"};
    auto view2 = fsv::filtered_string_view{"abc"};
    CHECK(view1 <= view2);
    CHECK(view1 < view2);
}

TEST_CASE("< and <= operators for two views using custom predicate") {
    auto view1 = fsv::filtered_string_view{"abc", vowels_only};
    auto view2 = fsv::filtered_string_view{"aaa", vowels_only};
    CHECK(view1 <= view2);
    CHECK(view1 < view2);
}

TEST_CASE("< and <= operators for two views of different predicates") {
    auto view1 = fsv::filtered_string_view{"abcde"};
    auto view2 = fsv::filtered_string_view{"abcde", vowels_only};
    CHECK(view1 <= view2);
    CHECK(view1 < view2);
}

TEST_CASE(">= and <= operators for two views of different predicates") {
    auto view1 = fsv::filtered_string_view{"aeiou"};
    auto view2 = fsv::filtered_string_view{"aeiou", vowels_only};
    CHECK(view1 >= view2);
    CHECK(view1 <= view2);
}

TEST_CASE("<< output operator for default constructed view") {
    auto view = fsv::filtered_string_view{};
    auto oss = std::ostringstream{};
    oss << view;
    CHECK(oss.str() == "");
}

TEST_CASE("<< output operator for empty view") {
    auto view = fsv::filtered_string_view{""};
    auto oss = std::ostringstream{};
    oss << view;
    CHECK(oss.str() == "");
}

TEST_CASE("<< output operator for view using default predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test};
    auto oss = std::ostringstream{};
    oss << view;
    CHECK(oss.str() == "abcde");
}

TEST_CASE("<< output operator for view using custom predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    auto oss = std::ostringstream{};
    oss << view;
    CHECK(oss.str() == "ae");
}

TEST_CASE("<< output operator for view not satisfying predicate") {
    auto test = std::string{"sdfghjkl"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    auto oss = std::ostringstream{};
    oss << view;
    CHECK(oss.str() == "");
}

// =============================================================================================
// NON-MEMBER FUNCTIONS
// =============================================================================================
TEST_CASE("compose for vector with a single predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test};
    auto predicates = std::vector<fsv::filter>{vowels_only};

    auto compose_view = compose(view, predicates);
    CHECK(compose_view.data() == test.data());
    auto str = static_cast<std::string>(compose_view);
    CHECK(str == "ae");
    for (auto c : {'a', 'Z', '0', '?', ' '}) {
        CHECK(compose_view.predicate()(c) == vowels_only(c));
    }
}

TEST_CASE("compose for vector of custom predicates") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test};
    auto predicates = std::vector<fsv::filter>{
        [](const char& c) { return c != 'b'; },
        [](const char& c) { return c != 'c'; },
        [](const char& c) { return c != 'd'; },
    };
    auto combo_pred = std::function<bool(const char&)>([](const char&) { return true; });
    for (auto p : predicates) {
        combo_pred = [combo_pred, p](const char& c) { return combo_pred(c) && p(c); };
    }

    auto compose_view = compose(view, predicates);
    CHECK(compose_view.data() == test.data());
    auto str = static_cast<std::string>(compose_view);
    CHECK(str == "ae");
    for (auto c : {'a', 'Z', '0', '?', ' '}) {
        CHECK(compose_view.predicate()(c) == combo_pred(c));
    }
}

TEST_CASE("compose replaces an existing predicate which is valid") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    auto predicates = std::vector<fsv::filter>{
        [](const char& c) { return c != 'a'; },
        [](const char& c) { return c != 'c'; },
        [](const char& c) { return c != 'e'; },
    };
    auto combo_pred = std::function<bool(const char&)>([](const char&) { return true; });
    for (auto p : predicates) {
        combo_pred = [combo_pred, p](const char& c) { return combo_pred(c) && p(c); };
    }

    auto compose_view = compose(view, predicates);
    CHECK(compose_view.data() == test.data());
    auto str = static_cast<std::string>(compose_view);
    CHECK(str == "bd");
    for (auto c : {'a', 'Z', '0', '?', ' '}) {
        CHECK(compose_view.predicate()(c) == combo_pred(c));
    }
}

TEST_CASE("compose replaces an existing predicate that isnt satisfied by the view") {
    auto test = std::string{"sdfghjkl"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    auto predicates = std::vector<fsv::filter>{
        [](const char& c) { return c != 'a'; },
        [](const char& c) { return c != 'c'; },
        [](const char& c) { return c != 'e'; },
    };
    auto combo_pred = std::function<bool(const char&)>([](const char&) { return true; });
    for (auto p : predicates) {
        combo_pred = [combo_pred, p](const char& c) { return combo_pred(c) && p(c); };
    }

    auto compose_view = fsv::compose(view, predicates);
    CHECK(compose_view.data() == test.data());
    auto str = static_cast<std::string>(compose_view);
    CHECK(str == "sdfghjkl");
    for (auto c : {'a', 'Z', '0', '?', ' '}) {
        CHECK(compose_view.predicate()(c) == combo_pred(c));
    }
}

TEST_CASE("compose short circuits after false predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    auto predicates = std::vector<fsv::filter>{
        always_false,
        [](const char& c) { return c != 'a'; },
        [](const char& c) { return c != 'e'; },
    };
    auto combo_pred = std::function<bool(const char&)>([](const char&) { return true; });
    for (auto p : predicates) {
        combo_pred = [combo_pred, p](const char& c) { return combo_pred(c) && p(c); };
    }

    auto compose_view = compose(view, predicates);
    CHECK(compose_view.data() == test.data());
    auto str = static_cast<std::string>(compose_view);
    CHECK(str == "");
    for (auto c : {'a', 'Z', '0', '?', ' '}) {
        CHECK(compose_view.predicate()(c) == combo_pred(c));
    }
}

TEST_CASE("split for view using default predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test};
    auto tok = fsv::filtered_string_view{"c"};

    auto split_view = split(view, tok);
    CHECK(split_view.size() == 2);
    CHECK(split_view[0].data() == test.data());
    CHECK(split_view[1].data() == test.data());
    auto str = static_cast<std::string>(split_view[0]);
    CHECK(str == "ab");
    str = static_cast<std::string>(split_view[1]);
    CHECK(str == "de");
}

TEST_CASE("split for view using custom predicate") {
    auto test = std::string{"aeiou"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    auto tok = fsv::filtered_string_view{"i", vowels_only};

    auto split_view = split(view, tok);
    CHECK(split_view.size() == 2);
    CHECK(split_view[0].data() == test.data());
    CHECK(split_view[1].data() == test.data());
    auto str = static_cast<std::string>(split_view[0]);
    CHECK(str == "ae");
    str = static_cast<std::string>(split_view[1]);
    CHECK(str == "ou");
}

TEST_CASE("split for empty view returns copy of view") {
    auto test = std::string{""};
    auto view = fsv::filtered_string_view{test};
    auto tok = fsv::filtered_string_view{"i"};

    auto split_view = split(view, tok);
    CHECK(split_view.size() == 1);
    CHECK(split_view[0].data() == test.data());
    auto str = static_cast<std::string>(split_view[0]);
    CHECK(str == "");
}

TEST_CASE("split for view not satisfying predicate returns copy of view") {
    auto test = std::string{"sdfghjkl"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    auto tok = fsv::filtered_string_view{"i"};

    auto split_view = split(view, tok);
    CHECK(split_view.size() == 1);
    CHECK(split_view[0].data() == test.data());
    auto str = static_cast<std::string>(split_view[0]);
    CHECK(str == "");
}

TEST_CASE("split for empty token returns copy of view") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test};
    auto tok = fsv::filtered_string_view{""};

    auto split_view = split(view, tok);
    CHECK(split_view.size() == 1);
    CHECK(split_view[0].data() == test.data());
    auto str = static_cast<std::string>(split_view[0]);
    CHECK(str == "abcde");
}

TEST_CASE("split for token not satisfying predicate returns copy of view") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test};
    auto tok = fsv::filtered_string_view{"c", vowels_only};

    auto split_view = split(view, tok);
    CHECK(split_view.size() == 1);
    CHECK(split_view[0].data() == test.data());
    auto str = static_cast<std::string>(split_view[0]);
    CHECK(str == "abcde");
}

TEST_CASE("split for view not containing token returns copy of view") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test};
    auto tok = fsv::filtered_string_view{"i"};

    auto split_view = split(view, tok);
    CHECK(static_cast<int>(split_view.size()) == 1);
    CHECK(split_view[0].data() == test.data());
    auto str = static_cast<std::string>(split_view[0]);
    CHECK(str == "abcde");
}

TEST_CASE("split for view containing more than one instance of token") {
    auto test = std::string{"aeiouaeiou"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    auto tok = fsv::filtered_string_view{"i"};

    auto split_view = split(view, tok);
    CHECK(split_view.size() == 3);
    CHECK(split_view[0].data() == test.data());
    CHECK(split_view[1].data() == test.data());
    CHECK(split_view[2].data() == test.data());
    auto str = static_cast<std::string>(split_view[0]);
    CHECK(str == "ae");
    str = static_cast<std::string>(split_view[1]);
    CHECK(str == "ouae");
    str = static_cast<std::string>(split_view[2]);
    CHECK(str == "ou");
}

TEST_CASE("split for view where token found at the start") {
    auto test = std::string{"aeiou"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    auto tok = fsv::filtered_string_view{"a"};

    auto split_view = split(view, tok);
    CHECK(split_view.size() == 2);
    CHECK(split_view[0].data() == test.data());
    CHECK(split_view[1].data() == test.data());
    auto str = static_cast<std::string>(split_view[0]);
    CHECK(str == "");
    str = static_cast<std::string>(split_view[1]);
    CHECK(str == "eiou");
}

TEST_CASE("split for view where token found at the end") {
    auto test = std::string{"aeiou"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    auto tok = fsv::filtered_string_view{"u"};

    auto split_view = split(view, tok);
    CHECK(split_view.size() == 2);
    CHECK(split_view[0].data() == test.data());
    CHECK(split_view[1].data() == test.data());
    auto str = static_cast<std::string>(split_view[0]);
    CHECK(str == "aeio");
    str = static_cast<std::string>(split_view[1]);
    CHECK(str == "");
}

TEST_CASE("split for view where token found twice in a row") {
    auto test = std::string{"aeiiou"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    auto tok = fsv::filtered_string_view{"i"};

    auto split_view = split(view, tok);
    CHECK(split_view.size() == 3);
    CHECK(split_view[0].data() == test.data());
    CHECK(split_view[1].data() == test.data());
    CHECK(split_view[2].data() == test.data());
    auto str = static_cast<std::string>(split_view[0]);
    CHECK(str == "ae");
    str = static_cast<std::string>(split_view[1]);
    CHECK(str == "");
    str = static_cast<std::string>(split_view[2]);
    CHECK(str == "ou");
}

TEST_CASE("substr for empty view is invalid") {
    auto view = fsv::filtered_string_view{""};
    CHECK_THROWS_AS(substr(view, 1), std::out_of_range);
}

TEST_CASE("substr for view using default predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test};

    auto substr_view = substr(view, 2);
    CHECK(substr_view.data() == test.data());
    auto str = static_cast<std::string>(substr_view);
    CHECK(str == "cde");
}

TEST_CASE("substr for view using custom predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test, vowels_only};

    auto substr_view = substr(view, 1);
    CHECK(substr_view.data() == test.data());
    auto str = static_cast<std::string>(substr_view);
    CHECK(str == "e");
}

TEST_CASE("substr for view using default predicate gives empty substring") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test};
    auto substr_view = substr(view, 5);
    CHECK(substr_view.data() == test.data());
    auto str = static_cast<std::string>(substr_view);
    CHECK(str == "");
}

TEST_CASE("substr for view using custom predicate gives empty substring") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    auto substr_view = substr(view, 2);
    CHECK(substr_view.data() == test.data());
    auto str = static_cast<std::string>(substr_view);
    CHECK(str == "");
}

TEST_CASE("substr for view using default predicate is out of bounds") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test};
    CHECK_THROWS_AS(substr(view, 6), std::out_of_range);
}

TEST_CASE("substr for view using custom predicate is out of bounds") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    CHECK_THROWS_AS(substr(view, 3), std::out_of_range);
}

TEST_CASE("substr for view using default predicate and upper bound") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test};

    auto substr_view = substr(view, 1, 3);
    CHECK(substr_view.data() == test.data());
    auto str = static_cast<std::string>(substr_view);
    CHECK(str == "bcd");
}

TEST_CASE("substr for view using custom predicate and upper bound") {
    auto test = std::string{"aeiou"};
    auto view = fsv::filtered_string_view{test, vowels_only};

    auto substr_view = substr(view, 1, 3);
    CHECK(substr_view.data() == test.data());
    auto str = static_cast<std::string>(substr_view);
    CHECK(str == "eio");
}

TEST_CASE("substr for view using default predicate and upper bound resolves to size") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test};
    auto substr_view = substr(view, 3, 3);
    CHECK(substr_view.data() == test.data());
    auto str = static_cast<std::string>(substr_view);
    CHECK(str == "de");
}

TEST_CASE("substr for view using custom predicate and upper bound resolves to size") {
    auto test = std::string{"aeiou"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    auto substr_view = substr(view, 3, 3);
    CHECK(substr_view.data() == test.data());
    auto str = static_cast<std::string>(substr_view);
    CHECK(str == "ou");
}

TEST_CASE("substr for view not satisfying custom predicate is invalid") {
    auto test = std::string{"sdfghjkl"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    CHECK_THROWS_AS(substr(view, 1), std::out_of_range);
}

// =============================================================================================
// ITERATORS
// =============================================================================================
TEST_CASE("fsviter default constructor compares equal to itself") {
    auto it = fsv::filtered_string_view::fsviter{};
    CHECK(it == it);
}

TEST_CASE("fsviter equal begin and end with empty view") {
    auto view = fsv::filtered_string_view{""};
    CHECK(view.begin() == view.end());
}

TEST_CASE("fsviter not equal begin and end with view using default predicate") {
    auto test = std::string{"abc"};
    auto view = fsv::filtered_string_view{test};
    CHECK(view.begin() != view.end());

    auto it = view.begin();
    CHECK(*it == 'a');
    ++it;
    CHECK(*it != 'a');
    CHECK(*it == 'b');
    ++it;
    CHECK(*it != 'b');
    CHECK(*it == 'c');
    ++it;
    CHECK(it == view.end());
}

TEST_CASE("fsviter not equal begin and end with view using custom predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    CHECK(view.begin() != view.end());

    auto it = view.begin();
    CHECK(*it == 'a');
    ++it;
    CHECK(*it == 'e');
    ++it;
    CHECK(it == view.end());
}

TEST_CASE("fsviter equal begin and end with view not satisfying custom predicate") {
    auto test = std::string{"sdfghjkl"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    CHECK(view.begin() == view.end());
}

TEST_CASE("fsviter equal begin and end with always false predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test, always_false};
    CHECK(view.begin() == view.end());
}

TEST_CASE("fsviter equal cbegin and cend with empty view") {
    auto view = fsv::filtered_string_view{""};
    CHECK(view.cbegin() == view.cend());
}

TEST_CASE("fsviter not equal cbegin and cend with view using default predicate") {
    auto test = std::string{"abc"};
    auto view = fsv::filtered_string_view{test};
    CHECK(view.cbegin() != view.cend());

    auto it = view.cbegin();
    CHECK(*it == 'a');
    ++it;
    CHECK(*it != 'a');
    CHECK(*it == 'b');
    ++it;
    CHECK(*it != 'b');
    CHECK(*it == 'c');
    ++it;
    CHECK(it == view.cend());
}

TEST_CASE("fsviter not equal cbegin and cend with view using custom predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    CHECK(view.cbegin() != view.cend());

    auto it = view.cbegin();
    CHECK(*it == 'a');
    ++it;
    CHECK(*it == 'e');
    ++it;
    CHECK(it == view.cend());
}

TEST_CASE("fsviter equal cbegin and cend with always false predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test, always_false};
    CHECK(view.cbegin() == view.cend());
}

TEST_CASE("fsviter equal rbegin and rend with empty view") {
    auto view = fsv::filtered_string_view{""};
    CHECK(view.rbegin() == view.rend());
}

TEST_CASE("fsviter not equal rbegin and rend with view using default predicate") {
    auto test = std::string{"abc"};
    auto view = fsv::filtered_string_view{test};
    CHECK(view.rbegin() != view.rend());

    auto it = view.rbegin();
    CHECK(*it == 'c');
    ++it;
    CHECK(*it != 'c');
    CHECK(*it == 'b');
    ++it;
    CHECK(*it != 'b');
    CHECK(*it == 'a');
    ++it;
    CHECK(it == view.rend());
}

TEST_CASE("fsviter not equal rbegin and rend with view using custom predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    CHECK(view.rbegin() != view.rend());

    auto it = view.rbegin();
    CHECK(*it == 'e');
    ++it;
    CHECK(*it == 'a');
    ++it;
    CHECK(it == view.rend());
}

TEST_CASE("fsviter equal rbegin and rend with view not satisfying custom predicate") {
    auto test = std::string{"sdfghjkl"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    CHECK(view.rbegin() == view.rend());
}

TEST_CASE("fsviter equal rbegin and rend with always false predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test, always_false};
    CHECK(view.rbegin() == view.rend());
}

TEST_CASE("fsviter equal crbegin and crend with empty view") {
    auto view = fsv::filtered_string_view{""};
    CHECK(view.crbegin() == view.crend());
}

TEST_CASE("fsviter not equal crbegin and crend with view using default predicate") {
    auto test = std::string{"abc"};
    auto view = fsv::filtered_string_view{test};
    CHECK(view.crbegin() != view.crend());

    auto it = view.crbegin();
    CHECK(*it == 'c');
    ++it;
    CHECK(*it != 'c');
    CHECK(*it == 'b');
    ++it;
    CHECK(*it != 'b');
    CHECK(*it == 'a');
    ++it;
    CHECK(it == view.crend());
}

TEST_CASE("fsviter not equal crbegin and crend with view using custom predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    CHECK(view.crbegin() != view.crend());

    auto it = view.crbegin();
    CHECK(*it == 'e');
    ++it;
    CHECK(*it == 'a');
    ++it;
    CHECK(it == view.crend());
}

TEST_CASE("fsviter equal crbegin and crend with always false predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test, always_false};
    CHECK(view.crbegin() == view.crend());
}

TEST_CASE("fsviter ++ and -- with view using default predicate") {
    auto test = std::string{"abc"};
    auto view = fsv::filtered_string_view{test};
    CHECK(view.begin() != view.end());

    auto it = view.begin();
    CHECK(*it == 'a');
    ++it;
    CHECK(*it == 'b');
    --it;
    CHECK(*it == 'a');
    ++it;
    ++it;
    ++it;
    CHECK(it == view.end());
    --it;
    --it;
    --it;
    CHECK(*it == 'a');
    CHECK(it == view.begin());
}

TEST_CASE("fsviter ++ and -- with view using custom predicate") {
    auto test = std::string{"abcde"};
    auto view = fsv::filtered_string_view{test, vowels_only};
    CHECK(view.begin() != view.end());

    auto it = view.begin();
    CHECK(*it == 'a');
    ++it;
    CHECK(*it == 'e');
    --it;
    CHECK(*it == 'a');
    ++it;
    ++it;
    CHECK(it == view.end());
    --it;
    --it;
    CHECK(*it == 'a');
    CHECK(it == view.begin());
}
