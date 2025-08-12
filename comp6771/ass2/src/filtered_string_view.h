#ifndef COMP6771_ASS2_FSV_H
#define COMP6771_ASS2_FSV_H

#include <compare>
#include <functional>
#include <iterator>
#include <optional>
#include <ostream>
#include <string>

namespace fsv {
    using filter = std::function<bool(const char&)>;

    class filtered_string_view {
    public:
        static filter default_predicate;

        class fsviter {
        public:
            using iterator_category = std::bidirectional_iterator_tag;
            using value_type = char;
            using reference = const char&;
            using pointer = void; // we're not using this
            using difference_type = std::ptrdiff_t;

            fsviter() = default;

            fsviter(const char* ptr, const filtered_string_view* view)
            : currptr_(ptr)
            , fsview_(view){};

            auto operator*() const noexcept -> reference;
            auto operator++() noexcept -> fsviter&;
            auto operator++(int) noexcept -> fsviter;
            auto operator--() noexcept -> fsviter&;
            auto operator--(int) noexcept -> fsviter;

            friend auto operator==(const fsviter& lhs, const fsviter& rhs) noexcept -> bool;
            friend auto operator!=(const fsviter& lhs, const fsviter& rhs) noexcept -> bool;

        private:
            /* Implementation-specific helper functions*/

            /* Implementation-specific private members */
            const char* currptr_{nullptr};
            const filtered_string_view* fsview_{nullptr};
        };

        using iterator = fsviter;
        using const_iterator = fsviter;
        using reverse_iterator = std::reverse_iterator<iterator>;
        using const_reverse_iterator = std::reverse_iterator<const_iterator>;

        filtered_string_view()
        : s_(nullptr)
        , length_(0)
        , pred_(default_predicate) {}

        filtered_string_view(const char* str)
        : s_(str)
        , length_(std::char_traits<char>::length(str))
        , pred_(default_predicate) {}

        filtered_string_view(const char* str, filter pred)
        : s_(str)
        , length_(
              static_cast<std::size_t>(std::count_if(std::string_view{str}.begin(), std::string_view{str}.end(), pred)))
        , pred_(pred) {}

        filtered_string_view(const std::string& str)
        : filtered_string_view(str.c_str()) {}

        filtered_string_view(const std::string& str, filter pred)
        : filtered_string_view(str.c_str(), pred) {}

        filtered_string_view(const filtered_string_view& other)
        : s_(other.s_)
        , length_(other.length_)
        , pred_(other.pred_) {}

        filtered_string_view(filtered_string_view&& other) noexcept;

        auto data() const noexcept -> const char*;
        auto predicate() const noexcept -> const filter&;
        auto size() const noexcept -> std::size_t;
        auto empty() const noexcept -> bool;
        auto at(std::size_t index) const -> const char&;

        auto operator=(const filtered_string_view& other) -> filtered_string_view&;
        auto operator=(filtered_string_view&& other) noexcept -> filtered_string_view&;
        auto operator[](size_t n) const noexcept -> const char&;
        explicit operator std::string() const;

        // non-member operators and functioons
        friend auto operator<=>(const filtered_string_view& lhs, const filtered_string_view& rhs) -> std::strong_ordering;
        friend auto operator==(const filtered_string_view& lhs, const filtered_string_view& rhs) -> bool;
        friend auto operator!=(const filtered_string_view& lhs, const filtered_string_view& rhs) -> bool;
        friend auto operator>(const filtered_string_view& lhs, const filtered_string_view& rhs) -> bool;
        friend auto operator<(const filtered_string_view& lhs, const filtered_string_view& rhs) -> bool;
        friend auto operator>=(const filtered_string_view& lhs, const filtered_string_view& rhs) -> bool;
        friend auto operator<=(const filtered_string_view& lhs, const filtered_string_view& rhs) -> bool;
        friend auto operator<<(std::ostream& os, const filtered_string_view& fsv) -> std::ostream&;

        // iterators
        auto begin() noexcept -> iterator;
        auto end() noexcept -> iterator;
        auto begin() const noexcept -> const_iterator;
        auto end() const noexcept -> const_iterator;
        auto cbegin() const noexcept -> const_iterator;
        auto cend() const noexcept -> const_iterator;
        auto rbegin() noexcept -> reverse_iterator;
        auto rend() noexcept -> reverse_iterator;
        auto rbegin() const noexcept -> const_reverse_iterator;
        auto rend() const noexcept -> const_reverse_iterator;
        auto crbegin() const noexcept -> const_reverse_iterator;
        auto crend() const noexcept -> const_reverse_iterator;

        ~filtered_string_view() noexcept = default;

    private:
        /* Implementation-specific helper functions*/

        /* Implementation-specific private members */
        const char* s_;
        std::size_t length_;
        filter pred_;
    };

    auto compose(const filtered_string_view& fsv, const std::vector<filter>& filts) -> filtered_string_view;
    auto split(const filtered_string_view& fsv, const filtered_string_view& tok) -> std::vector<filtered_string_view>;
    auto substr(const filtered_string_view& fsv, size_t pos = 0, std::optional<size_t> count = std::nullopt)
        -> filtered_string_view;

} // namespace fsv

#endif // COMP6771_ASS2_FSV_H
