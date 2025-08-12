#include "./filtered_string_view.h"

#include <compare>
#include <cstring>
#include <vector>

namespace fsv {
    filter filtered_string_view::default_predicate = [](const char&) -> bool { return true; };

    filtered_string_view::filtered_string_view(filtered_string_view&& other) noexcept
    : s_(other.s_)
    , length_(other.length_)
    , pred_(std::move(other.pred_)) {
        other.s_ = nullptr;
        other.length_ = 0;
        other.pred_ = filtered_string_view::default_predicate;
    }

    auto filtered_string_view::data() const noexcept -> const char* {
        return s_;
    }

    auto filtered_string_view::predicate() const noexcept -> const filter& {
        return pred_;
    }

    auto filtered_string_view::size() const noexcept -> std::size_t {
        return length_;
    }

    auto filtered_string_view::empty() const noexcept -> bool {
        return filtered_string_view::size() == 0;
    }

    auto filtered_string_view::operator=(const filtered_string_view& other) -> filtered_string_view& {
        if (this != &other) {
            s_ = other.s_;
            length_ = other.length_;
            pred_ = other.pred_;
        }
        return *this;
    }

    auto filtered_string_view::operator=(filtered_string_view&& other) noexcept -> filtered_string_view& {
        if (this != &other) {
            s_ = other.s_;
            length_ = other.length_;
            pred_ = other.pred_;
            other.s_ = nullptr;
            other.length_ = 0;
            other.pred_ = default_predicate;
        }
        return *this;
    }

    auto filtered_string_view::operator[](std::size_t n) const noexcept -> const char& {
        auto it = begin();
        std::advance(it, static_cast<filtered_string_view::fsviter::difference_type>(n));
        return *it;
    }

    auto filtered_string_view::at(std::size_t index) const -> const char& {
        if (index >= length_) {
            throw std::domain_error("filtered_string_view::at(" + std::to_string(index) + "): invalid index");
        }
        return filtered_string_view::operator[](index);
    }

    filtered_string_view::operator std::string() const {
        return std::string{begin(), end()};
    }

    auto operator<=>(const filtered_string_view& lhs, const filtered_string_view& rhs) -> std::strong_ordering {
        return std::string(lhs) <=> std::string(rhs);
    }

    auto operator==(const filtered_string_view& lhs, const filtered_string_view& rhs) -> bool {
        return (lhs <=> rhs) == std::strong_ordering::equal;
    }

    auto operator!=(const filtered_string_view& lhs, const filtered_string_view& rhs) -> bool {
        return !(lhs == rhs);
    }

    auto operator>(const filtered_string_view& lhs, const filtered_string_view& rhs) -> bool {
        return (lhs <=> rhs) == std::strong_ordering::greater;
    }

    auto operator<(const filtered_string_view& lhs, const filtered_string_view& rhs) -> bool {
        return (lhs <=> rhs) == std::strong_ordering::less;
    }

    auto operator>=(const filtered_string_view& lhs, const filtered_string_view& rhs) -> bool {
        return !(lhs < rhs);
    }

    auto operator<=(const filtered_string_view& lhs, const filtered_string_view& rhs) -> bool {
        return !(lhs > rhs);
    }

    auto operator<<(std::ostream& os, const filtered_string_view& fsv) -> std::ostream& {
        if (fsv.s_)
            os << std::string(fsv);
        return os;
    }

    auto compose(const filtered_string_view& fsv, const std::vector<filter>& filts) -> filtered_string_view {
        auto new_pred = std::function<bool(const char&)>([](const char&) { return true; });
        for (const auto& filter : filts) {
            new_pred = [new_pred, filter](const char& c) { return new_pred(c) && filter(c); };
        }
        return filtered_string_view(fsv.data(), new_pred);
    }

    auto split(const filtered_string_view& fsv, const filtered_string_view& tok) -> std::vector<filtered_string_view> {
        const auto filtered_str = static_cast<std::string>(fsv);
        const auto tok_str = static_cast<std::string>(tok);
        auto res = std::vector<filtered_string_view>{};
        if (tok_str.empty()) {
            res.emplace_back(fsv.data(), fsv.predicate());
            return res;
        }

        auto start_pos = size_t{0};
        while (true) {
            const auto pos = filtered_str.find(tok_str, start_pos);

            if (pos == std::string::npos) {
                res.emplace_back(substr(fsv, start_pos));
                break;
            }

            const auto count = pos - start_pos;
            res.emplace_back(substr(fsv, start_pos, count));
            start_pos = pos + tok_str.size();
        }

        return res;
    }

    auto substr(const filtered_string_view& fsv, size_t pos, std::optional<size_t> count) -> filtered_string_view {
        const auto size = fsv.size();
        if (pos > size) {
            throw std::out_of_range("filtered_string_view::substr(" + std::to_string(pos)
                                    + "): position out of range for filtered string of size " + std::to_string(size));
        }
        const auto end = count.has_value() ? std::min(pos + count.value(), size) : size;

        // build wrapper around predicate to only accept characters between start and end
        auto allowed_indices = std::vector<size_t>{};
        auto filtered_idx = size_t{0};
        for (auto i = size_t{0}; fsv.data()[i] != '\0'; ++i) {
            if (fsv.predicate()(fsv.data()[i])) {
                if (filtered_idx >= pos && filtered_idx < end)
                    allowed_indices.push_back(i);
                ++filtered_idx;
            }
        }

        const auto base_ptr = fsv.data();
        const auto new_pred = [base_pred = fsv.predicate(), base_ptr, allowed = std::move(allowed_indices)](const char& c) {
            const auto offset = static_cast<size_t>(&c - base_ptr);
            return base_pred(c) && std::find(allowed.begin(), allowed.end(), offset) != allowed.end();
        };

        return filtered_string_view(fsv.data(), new_pred);
    }

    auto filtered_string_view::begin() noexcept -> iterator {
        if (!s_)
            return iterator{nullptr, this};
        auto ptr = s_;
        while (*ptr != '\0' && !pred_(*ptr)) {
            ++ptr;
        }
        if (*ptr == '\0') {
            return end();
        }
        return iterator{ptr, this};
    }

    auto filtered_string_view::end() noexcept -> iterator {
        if (!s_)
            return iterator{nullptr, this};
        auto ptr = s_ + strlen(s_);
        return iterator{ptr, this};
    }

    auto filtered_string_view::begin() const noexcept -> const_iterator {
        if (!s_)
            return const_iterator{nullptr, this};
        auto ptr = s_;
        while (*ptr != '\0' && !pred_(*ptr)) {
            ++ptr;
        }
        if (*ptr == '\0') {
            return end();
        }
        return const_iterator{ptr, this};
    }

    auto filtered_string_view::end() const noexcept -> const_iterator {
        if (!s_)
            return const_iterator{nullptr, this};
        auto ptr = s_ + strlen(s_);
        return const_iterator{ptr, this};
    }

    auto filtered_string_view::cbegin() const noexcept -> const_iterator {
        return filtered_string_view::begin();
    }

    auto filtered_string_view::cend() const noexcept -> const_iterator {
        return filtered_string_view::end();
    }

    auto filtered_string_view::rbegin() noexcept -> reverse_iterator {
        return std::reverse_iterator<iterator>(filtered_string_view::end());
    }

    auto filtered_string_view::rend() noexcept -> reverse_iterator {
        return std::reverse_iterator<iterator>(filtered_string_view::begin());
    }

    auto filtered_string_view::rbegin() const noexcept -> const_reverse_iterator {
        return std::reverse_iterator<const_iterator>(filtered_string_view::end());
    }

    auto filtered_string_view::rend() const noexcept -> const_reverse_iterator {
        return std::reverse_iterator<const_iterator>(filtered_string_view::begin());
    }

    auto filtered_string_view::crbegin() const noexcept -> const_reverse_iterator {
        return filtered_string_view::rbegin();
    }

    auto filtered_string_view::crend() const noexcept -> const_reverse_iterator {
        return filtered_string_view::rend();
    }

    auto filtered_string_view::fsviter::operator*() const noexcept -> filtered_string_view::fsviter::reference {
        return *currptr_;
    }

    auto filtered_string_view::fsviter::operator++() noexcept -> filtered_string_view::fsviter& {
        if (!currptr_ || !fsview_)
            return *this;
        do {
            ++currptr_;
        } while (*currptr_ != '\0' && !fsview_->pred_(*currptr_));
        return *this;
    }

    auto filtered_string_view::fsviter::operator++(int) noexcept -> filtered_string_view::fsviter {
        auto temp = *this;
        ++(*this);
        return temp;
    }

    auto filtered_string_view::fsviter::operator--() noexcept -> filtered_string_view::fsviter& {
        if (!currptr_ || !fsview_)
            return *this;
        do {
            --currptr_;
        } while (currptr_ >= fsview_->s_ && !fsview_->pred_(*currptr_));
        return *this;
    }

    auto filtered_string_view::fsviter::operator--(int) noexcept -> filtered_string_view::fsviter {
        auto temp = *this;
        --(*this);
        return temp;
    }

    auto operator==(const filtered_string_view::fsviter& lhs, const filtered_string_view::fsviter& rhs) noexcept -> bool {
        return lhs.currptr_ == rhs.currptr_ && lhs.fsview_ == rhs.fsview_;
    }

    auto operator!=(const filtered_string_view::fsviter& lhs, const filtered_string_view::fsviter& rhs) noexcept -> bool {
        return !(lhs == rhs);
    }

} // namespace fsv
